package com.example.sorrentix.palmy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainAcitivty";

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
    public void donna(View v) {
        Intent camera = new Intent(this, CameraActivity.class);
        camera.putExtra("mano","sx");
        startActivity(camera);
    }

    public void uomo(View v) {
        Intent camera = new Intent(this, CameraActivity.class);
        camera.putExtra("mano","dx");
        startActivity(camera);
    }


}
