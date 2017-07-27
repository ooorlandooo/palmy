package com.example.sorrentix.palmy;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.sorrentix.palmy.R.id.camera_preview;
import static com.example.sorrentix.palmy.R.id.image;

public class CameraActivity extends Activity implements TextureView.SurfaceTextureListener {
    private Camera mCamera;
    private TextureView mTextureView;
    private int mask;
    private int mask_piena;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            permissionsHandler();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void permissionsHandler() throws IOException {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 123);

                }

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 123);

                }
            }
        }

    }

    public void takeScreenShot(View v) {
        Bitmap bmp = mTextureView.getBitmap();
        Bitmap bmp2 = BitmapFactory.decodeResource(this.getResources(),mask_piena);
        Toast.makeText(this, "CHIAMO IL SALVATAGGIO", Toast.LENGTH_SHORT).show();
        ImageUtils.mergeAndSave(bmp,bmp2);
        Toast.makeText(this, "FINE SALVATAGGIO", Toast.LENGTH_SHORT).show();
    }
}