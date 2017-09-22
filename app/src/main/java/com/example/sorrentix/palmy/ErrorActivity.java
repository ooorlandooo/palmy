package com.example.sorrentix.palmy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_error);
        this.setFinishOnTouchOutside(false);
    }
    public void launchCamera(View v) {
        Intent camera = new Intent(this, CameraActivity.class);
        startActivity(camera);
    }
}
