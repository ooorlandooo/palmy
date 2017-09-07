package com.example.sorrentix.palmy;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class CameraActivity extends Activity implements SurfaceHolder.Callback,
                                                        MediaScannerConnection.OnScanCompletedListener,
                                                        Camera.PictureCallback{
    private static final String TAG = "CameraActivity";

    private SurfaceView cameraView, transparentView;
    private SurfaceHolder holder, holderTransparent;

    private int deviceWidth;
    private int deviceHeight;

    private Camera mCamera;

    private Camera.Size best_preview;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = (SurfaceView)findViewById(R.id.CameraView);
        holder = cameraView.getHolder();
        holder.addCallback(this);
        cameraView.setSecure(true);

        transparentView = (SurfaceView)findViewById(R.id.TransparentView);
        holderTransparent = transparentView.getHolder();
        holderTransparent.addCallback(this);
        holderTransparent.setFormat(PixelFormat.TRANSLUCENT);
        transparentView.setZOrderMediaOverlay(true);

        deviceWidth = getScreenWidth();
        deviceHeight = getScreenHeight();
    }

    public void takeScreenShot(View v) {
        System.out.println("foto scattata");
        mCamera.takePicture(null,null,this);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            synchronized (holder){
                draw();
            }
            mCamera = Camera.open(); //open a camera
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }


        mCamera.setDisplayOrientation(90);
        Camera.Parameters params = mCamera.getParameters();

        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        List<Camera.Size> imageSizes = mCamera.getParameters().getSupportedPictureSizes();

        Camera.Size best_size = imageSizes.get(imageSizes.size()-1);
        for (Camera.Size imgSize : imageSizes){
            if (imgSize.height >= deviceWidth && imgSize.width >= (deviceHeight/2)+(deviceWidth/2)){
                best_size = imgSize;
                break;
            }
        }

        double aspect_ratio = best_size.height/best_size.width;
        double best_aspect_ratio_diff = 100;
        double temp_aspect_ratio = 0;
        List<Camera.Size> previewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        best_preview = previewSizes.get(imageSizes.size()-1);

        for (Camera.Size previewSize : previewSizes){
            temp_aspect_ratio = previewSize.height/previewSize.width;
            System.out.println("Dimensione delle merde:"+previewSize.width+" "+previewSize.height+" "+deviceWidth);
            if (previewSize.height >= deviceWidth && previewSize.width >= (deviceHeight/2)+(deviceWidth/2) && (Math.abs(aspect_ratio-temp_aspect_ratio)<=best_aspect_ratio_diff)){
                best_aspect_ratio_diff = Math.abs(aspect_ratio-temp_aspect_ratio);
                best_preview = previewSize;
                if (best_aspect_ratio_diff == 0) break;
            }
        }



        System.out.println("Dimensione delle immagini:"+best_size);
        params.setPreviewSize(best_preview.width,best_preview.height);
        params.setPictureSize(best_size.width,best_size.height);
        mCamera.setParameters(params);


        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera(); //call method for refresh camera
    }

    public void refreshCamera() {
        if (holder.getSurface() == null) {
            return;
        }

        try {
            mCamera.stopPreview();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.release(); //for release a camera
    }


    private int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }


    private int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    private void draw(){
        Canvas canvas = holderTransparent.lockCanvas(null);
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);

        Rect square = new Rect(1,((deviceHeight/2)-(deviceWidth/2)),deviceWidth-1,((deviceHeight/2)+(deviceWidth/2))-1); //new Rect((int) RectLeft,(int)RectTop,(int)RectRight,(int)RectBottom);
        canvas.drawRect(square,paint);

        holderTransparent.unlockCanvasAndPost(canvas);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Bitmap bmp = BitmapFactory.decodeByteArray(data,0,data.length);
        //System.out.println("Dimensioni immagine: "+bmp.getWidth()+" "+bmp.getHeight());
        //System.out.println("Dimensioni preview: "+best_preview.width+" "+best_preview.height);
        Bitmap resized_to_preview = Bitmap.createScaledBitmap(bmp,best_preview.width,best_preview.height,false);
        Button btn = (Button) findViewById(R.id.button_capture);

        Bitmap rotated = rotateImage(resized_to_preview,90); //checkAndRotate(,bmp);
        //System.out.println("mammt"+(deviceHeight/2-deviceWidth/2)+"  dw/2"+deviceWidth/2+" dh/2"+deviceHeight/2+" btn "+   btn.getHeight());
        //TODO aggiungere il controllo sulla rotazione delll'immagine e migliorare la precisione del taglio
        Bitmap cropped = Bitmap.createBitmap(rotated,1,(best_preview.width + btn.getHeight())/2-best_preview.height/2 ,deviceWidth-1,deviceWidth-1);
        Bitmap resized = Bitmap.createScaledBitmap(cropped,500,500,false);
        Toast.makeText(this, "CHIAMO IL SALVATAGGIO", Toast.LENGTH_SHORT).show();
        Uri fileUri = ImageUtils.mergeAndSave(resized,this);

        /*FileHandler fileHandler = new FileHandler();
        File imageFile = fileHandler.getOutputMediaFile(FileHandler.MEDIA_TYPE_IMAGE);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(imageFile);
            resized.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaScannerConnection.scanFile(this, new String[]{fileHandler.getUriFromFile(imageFile).getPath()}, null, this);*/
        MediaScannerConnection.scanFile(this, new String[]{fileUri.getPath()}, null, this);

        Toast.makeText(this, "FINE SALVATAGGIO", Toast.LENGTH_SHORT).show();
    }

/*
    public static Bitmap checkAndRotate(String photoPath, Bitmap bitmap){
        try {
            ExifInterface ei = null;
            ei = new ExifInterface() //new ExifInterface(photoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch(orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotateImage(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotateImage(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotateImage(bitmap, 270);
                    break;
            }
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
*/
    private static Bitmap rotateImage(Bitmap source, float angle) {
        Bitmap retVal;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return retVal;
    }
}