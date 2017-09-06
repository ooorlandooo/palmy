package com.example.sorrentix.palmy;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.IOException;


public class CameraActivity extends Activity implements TextureView.SurfaceTextureListener, MediaScannerConnection.OnScanCompletedListener {
    private static final String TAG = "CameraActivity";


    private Camera mCamera;
    private TextureView mTextureView;
    private int mask;
    private int mask_piena;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imgview = (ImageView) findViewById(R.id.image_view);
        String srcimg = getIntent().getExtras().getString("mano");

        if(srcimg.equals("sx")) {
            mask = R.drawable.mano_sx;
            mask_piena = R.drawable.mano_sx_piena;
        }
        else {
            mask = R.drawable.mano_dx;
            mask_piena = R.drawable.mano_dx_piena;
        }

        imgview.setImageResource(mask);

        mTextureView = (TextureView) findViewById(R.id.camera_preview);
        mTextureView.setSurfaceTextureListener(this);

    }



    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mCamera = Camera.open();
        try {
            mCamera.setDisplayOrientation(90);
            Camera.Parameters params = mCamera.getParameters();
            if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(params);
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException ioe) {}
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        return true;
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    public void takeScreenShot(View v) {
        Bitmap bmp = mTextureView.getBitmap();
        Bitmap bmp2 = BitmapFactory.decodeResource(this.getResources(),mask_piena);
        Toast.makeText(this, "CHIAMO IL SALVATAGGIO", Toast.LENGTH_SHORT).show();
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(params);
        Uri fileUri = ImageUtils.mergeAndSave(bmp,bmp2,this);
        MediaScannerConnection.scanFile(this, new String[]{fileUri.getPath()}, null, this);
        Toast.makeText(this, "FINE SALVATAGGIO", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {

    }
}