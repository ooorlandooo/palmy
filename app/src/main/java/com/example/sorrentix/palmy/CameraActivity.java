package com.example.sorrentix.palmy;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.sorrentix.palmy.util.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class CameraActivity extends Activity implements SurfaceHolder.Callback,
                                                        MediaScannerConnection.OnScanCompletedListener,
                                                        Camera.PictureCallback,
                                                        MyResultReceiver.Receiver {

    private static final String TAG = "CameraActivity";

    private SurfaceView cameraView, transparentView;
    private SurfaceHolder holder, holderTransparent;
    private Button btn;
    private int deviceWidth;
    private int deviceHeight;

    private Camera mCamera;

    private Camera.Size best_preview;

    private MyResultReceiver mReceiver;

    private FileHandler fileHandler = new FileHandler();
    private File imageFile;
    private String imagePath;


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

        btn = (Button) findViewById(R.id.button_capture);

        mReceiver = new MyResultReceiver(new Handler());
        mReceiver.setReceiver(this);
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
       // params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);


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
            //System.out.println("Dimensione delle merde:"+previewSize.width+" "+previewSize.height+" "+deviceWidth);
            if (previewSize.height >= deviceWidth && previewSize.width >= (deviceHeight/2)+(deviceWidth/2) && (Math.abs(aspect_ratio-temp_aspect_ratio)<=best_aspect_ratio_diff)){
                best_aspect_ratio_diff = Math.abs(aspect_ratio-temp_aspect_ratio);
                best_preview = previewSize;
                if (best_aspect_ratio_diff == 0) break;
            }
        }



        //System.out.println("Dimensione delle immagini:"+best_size);
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
        imageFile = fileHandler.getOutputMediaFile(FileHandler.MEDIA_TYPE_IMAGE);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(imageFile);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imagePath = fileHandler.getUriFromFile(imageFile).getPath();
        MediaScannerConnection.scanFile(this, new String[]{imagePath}, null, this);

        launchLinesExtractorService();//FINE

    }

    private void launchLinesExtractorService() {
        Toast.makeText(this, "CHIAMO IL SERVICE", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, LinesExtractorService.class);
        // Add extras to the bundle
        i.putExtra(Message.RECEIVER_TAG, mReceiver);
        i.putExtra(Message.IMG_PATH, imagePath);
        i.putExtra(Message.BTN_HEIGHT, btn.getHeight());
        i.putExtra(Message.PREVIEW_WIDTH, best_preview.width);
        i.putExtra(Message.PREVIEW_HEIGHT, best_preview.height);
        i.putExtra(Message.DEVICE_WIDTH, deviceWidth);
        i.putExtra(Message.NOTI_ICON, R.drawable.ic_stat_compare);
        // Start the service
        System.out.println("chiamo il service");
        startService(i);
        System.out.println("Dopo start service");
    }


    @Override//METODO IN CUI SARA' MOSTRATA L'IMMAGINE CON LE LINEE SOPRA, INVOCATO AL TERMINE DEL SERVICE
    public void onReceiveResult(int resultCode, Bundle resultData) {
        Toast.makeText(this, "FINE SERVICE", Toast.LENGTH_SHORT).show();
        if (resultCode == RESULT_OK) {
            String newImagePath = resultData.getString(Message.IMG_PATH);
            MediaScannerConnection.scanFile(this, new String[]{newImagePath}, null, this);
        }
    }
}