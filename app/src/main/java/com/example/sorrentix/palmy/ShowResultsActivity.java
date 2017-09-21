package com.example.sorrentix.palmy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowResultsActivity extends Activity {

    Bitmap bmp;
    String prediction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_results);

        Intent intent = getIntent();
        bmp = intent.getParcelableExtra("Bitmap");
        prediction = intent.getStringExtra("Prediction");

        ImageView imgv = (ImageView) findViewById(R.id.imageView);
        imgv.setImageBitmap(bmp);

        TextView txt = (TextView) findViewById(R.id.textView);
        txt.setText(prediction);
    }
}
