package com.example.sorrentix.palmy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.sorrentix.palmy.util.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@SuppressLint("ParcelCreator")
public class CameraActivity extends Activity implements SurfaceHolder.Callback,
                                                        MediaScannerConnection.OnScanCompletedListener,
                                                        Camera.PictureCallback,
                                                        MyResultReceiver.Receiver{

    private static final String TAG = "CameraActivity";

    private SurfaceView cameraView, transparentView;
    private SurfaceHolder holder, holderTransparent;
    private Button btn;
    private double deviceWidth;
    private double deviceHeight;

    private Camera mCamera;

    private Camera.Size best_preview;

    private MyResultReceiver mReceiver;

    private FileHandler fileHandler = new FileHandler();
    private File imageFile;
    private  String imagePath;
    private double scaledX;
    private double scaledY;
    private Camera.Size best_size;
    Drawable d;

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



        Bitmap bMap = BitmapFactory.decodeResource(getResources(),R.drawable.palm_icon);



        d = new BitmapDrawable(bMap);


        btn = (Button) findViewById(R.id.button_capture);

        mReceiver = new MyResultReceiver(new Handler());
        mReceiver.setReceiver(this);
    }

    public void takeScreenShot(View v) {
        mCamera.takePicture(null,null,this);

    }

    @Override
    public void onScanCompleted(String path, Uri uri) {}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        deviceWidth = cameraView.getWidth();
        deviceHeight = cameraView.getHeight();
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
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);


        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        List<Camera.Size> imageSizes = mCamera.getParameters().getSupportedPictureSizes();
        double surfaceAspect = deviceHeight/deviceWidth;
         best_size = imageSizes.get(imageSizes.size()-1);
        double temp_aspect_ratio = 0;
        double best_aspect_ratio_diff = 100;
        for (Camera.Size imgSize : imageSizes){
            temp_aspect_ratio = (double)(imgSize.width)/(double)(imgSize.height);
            if (imgSize.height >= deviceWidth && imgSize.width >= (deviceHeight/2)+(deviceWidth/2) && (Math.abs(surfaceAspect-temp_aspect_ratio)<=best_aspect_ratio_diff)){
                best_size = imgSize;
                best_aspect_ratio_diff = Math.abs(surfaceAspect-temp_aspect_ratio);
            }
        }
        scaledX = best_size.height/deviceWidth;
        scaledY = best_size.width/deviceHeight;
       double aspect_ratio = best_size.height/best_size.width;
         best_aspect_ratio_diff = 100;
        List<Camera.Size> previewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        best_preview = previewSizes.get(previewSizes.size()-1);

        for (Camera.Size previewSize : previewSizes){
            temp_aspect_ratio = previewSize.height/previewSize.width;
            if (previewSize.height >= deviceWidth && previewSize.width >= (deviceHeight/2)+(deviceWidth/2) && (Math.abs(aspect_ratio-temp_aspect_ratio)<=best_aspect_ratio_diff)){
                best_aspect_ratio_diff = Math.abs(aspect_ratio-temp_aspect_ratio);
                best_preview = previewSize;
              if (best_aspect_ratio_diff == 0) break;
            }
        }
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




    private void draw(){
        Canvas canvas = holderTransparent.lockCanvas(null);
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(5);
        System.out.println("DRAW DIM: "+deviceWidth+"*"+deviceHeight);
        System.out.println("REct x:"+(int)(deviceWidth/4)+" rect y:"+(int)((deviceHeight/2)-(deviceWidth/4))+" width:"+(int)(deviceWidth- deviceWidth/4)+"height"+(int)((deviceHeight/2)+(deviceWidth/4)));
        Rect square = new Rect((int)(deviceWidth/4),(int)((deviceHeight/2)-(deviceWidth/4)),(int)(deviceWidth- deviceWidth/4),(int)((deviceHeight/2)+(deviceWidth/4)));  //new Rect((int) RectLeft,(int)RectTop,(int)RectRight,(int)RectBottom);        canvas.drawRect(square,paint);
        canvas.drawRect(square,paint);
        d.setBounds((int)(deviceWidth/3),(int)(deviceHeight/14),(int)(deviceWidth- deviceWidth/3),(int)((deviceHeight/14)+(deviceWidth/3)));
        d.draw(canvas);
        paint.setTextSize(40);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawText("Inserisci il palmo della tua mano sinistra nel riquadro", (int)(deviceWidth/15), (int)((deviceHeight/2 + deviceHeight/3)), paint);
        holderTransparent.unlockCanvasAndPost(canvas);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(params);
        Bitmap bmp = BitmapFactory.decodeByteArray(data,0,data.length);
        imageFile = fileHandler.getOutputMediaFile(FileHandler.MEDIA_TYPE_IMAGE);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(imageFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imagePath = fileHandler.getUriFromFile(imageFile).getPath();
        MediaScannerConnection.scanFile(this, new String[]{imagePath}, null, this);
        launchLinesExtractorService();
        Intent loading = new Intent(this, LoadingActivity.class);
        startActivity(loading);


    }

    private void launchLinesExtractorService() {
        Intent i = new Intent(this, LinesExtractorService.class);
        // Add extras to the bundle
        i.putExtra(Message.RECEIVER_TAG, mReceiver);
        i.putExtra(Message.IMG_PATH, imagePath);
        i.putExtra(Message.BTN_HEIGHT, btn.getHeight());
        i.putExtra(Message.PREVIEW_WIDTH, best_size.width);
        i.putExtra(Message.PREVIEW_HEIGHT, best_size.height);
        i.putExtra(Message.DEVICE_WIDTH, deviceWidth);
        i.putExtra(Message.DEVICE_HEIGHT, deviceHeight);
        i.putExtra(Message.NOTI_ICON, R.drawable.ic_stat_compare);
        i.putExtra(Message.SCALEDX, scaledX);
        i.putExtra(Message.SCALEDY, scaledY);
        // Start the service
        startService(i);
    }


    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent mainActivity = new Intent(this, MainActivity.class);
        startActivity(mainActivity);
    }
}