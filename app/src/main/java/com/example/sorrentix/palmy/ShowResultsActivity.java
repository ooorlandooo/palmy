package com.example.sorrentix.palmy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

public class ShowResultsActivity extends Activity {

    Bitmap bmp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_results);

        Intent intent = getIntent();
        bmp = intent.getParcelableExtra("Bitmap");

        ImageView imgv = (ImageView) findViewById(R.id.imageView);
        imgv.setImageBitmap(bmp);
    }
}
