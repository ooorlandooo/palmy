package com.example.sorrentix.palmy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.opencv.android.OpenCVLoader;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainAcitivty";

    public  static final int PERMISSIONS_MULTIPLE_REQUEST = 123;


    static {
        if(OpenCVLoader.initDebug()){
            Log.d(TAG,"ok");
        }
        else {
            Log.d(TAG,"no");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
    }
    public void startCamera(View v) {
        checkPermissions();
    }

    private void startCameraActivity(){
        Intent camera = new Intent(this, CameraActivity.class);
        startActivity(camera);
    }

    private void checkPermissions(){
        try {
            permissionsHandler();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Permessi negati, abort app");
        }
    }


    private void  permissionsHandler() throws IOException{
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

                Snackbar.make(this.findViewById(android.R.id.content),
                        "Per favore, accetta i permessi richiesti dall'applicazione per usarla",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)//MANDATORY CHECK
                                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                                    Manifest.permission.CAMERA},
                                            PERMISSIONS_MULTIPLE_REQUEST);
                            }
                        }).show();
            } else {
                Log.e(TAG, "permissionsHandler: richiesta di permessi");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)//MANDATORY CHECK
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.CAMERA},
                            PERMISSIONS_MULTIPLE_REQUEST);
            }
        } else {
            Log.e(TAG, "permissionsHandler: permessi già in possesso");
            startCameraActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSIONS_MULTIPLE_REQUEST:
                if (grantResults.length > 0) {
                    boolean cameraPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean readExternalFile = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(cameraPermission && readExternalFile){
                        Log.e(TAG, "onRequestPermissionsResult: before loadStuff");
                        startCameraActivity();
                    } else {
                        Snackbar.make(this.findViewById(android.R.id.content),
                                "Please Grant Permissions to use application",
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)//MANDATORY CHECK
                                            requestPermissions(
                                                    new String[]{Manifest.permission
                                                            .READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                                                    PERMISSIONS_MULTIPLE_REQUEST);
                                    }
                                }).show();
                    }
                }
                break;
        }
    }

}
