package com.example.sorrentix.palmy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.example.sorrentix.palmy.util.ImageResizer;
import com.example.sorrentix.palmy.util.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static java.lang.Thread.sleep;

/**
 * Created by sorrentix on 09/09/2017.
 */

public class LinesExtractorService extends IntentService implements MediaScannerConnection.OnScanCompletedListener {

    private final static String TAG = "LinesExtractorService";

    private Bitmap bmp;
    private int imgWidth;
    private int imgHeight;
    private int btnHeight;
    private double deviceWidth;
    private double deviceHeight;
    private String imgPath;
    private Context context;
    private FileHandler fileHandler = new FileHandler();
    private File imageFile;
    private String imagePath;
    private double scaledX;
    private double scaledY;

    public LinesExtractorService() {
        super("LinesExtractorService");
    }


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public LinesExtractorService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Extract the receiver passed into the service
        int noti_icon = intent.getIntExtra(Message.NOTI_ICON, 0);
        Notification noti = new NotificationCompat.Builder(this)
                .setContentTitle("Servizio Palmy")
                .setContentText("Service is active and is processing the image")
                .setSmallIcon(noti_icon)
                .build();

        Log.d(TAG, "" + noti.toString());

        startForeground(101, noti);
        if (intent == null) return;
        ResultReceiver rec = intent.getParcelableExtra(Message.RECEIVER_TAG);
        // Extract additional values from the bundle
        imgWidth = intent.getIntExtra(Message.PREVIEW_WIDTH, 0);
        imgHeight = intent.getIntExtra(Message.PREVIEW_HEIGHT, 0);
        btnHeight = intent.getIntExtra(Message.BTN_HEIGHT, 0);
        deviceWidth = intent.getDoubleExtra(Message.DEVICE_WIDTH, 0);
        deviceHeight = intent.getDoubleExtra(Message.DEVICE_HEIGHT, 0);
        imgPath = intent.getStringExtra(Message.IMG_PATH);
        scaledX = intent.getDoubleExtra(Message.SCALEDX, 0);
        scaledY = intent.getDoubleExtra(Message.SCALEDY,0);

        bmp = ImageResizer.checkAndRotate(imgPath, ImageResizer.decodeSampledBitmapFromFile(imgPath, imgWidth, imgHeight, null));

        if (bmp == null) {
            rec.send(Activity.RESULT_CANCELED, new Bundle());
            return;
        }
        bmp = rotateImage(bmp, 90);Bitmap cropped;
        if(bmp.getWidth()>bmp.getHeight())
            cropped = Bitmap.createBitmap(bmp, (bmp.getHeight() / 4), (((bmp.getWidth()+(btnHeight/2)) / 2) - (bmp.getHeight() / 4)), (bmp.getWidth() / 2), (((bmp.getHeight()-btnHeight/2) / 2)-(btnHeight/2)));
        else
            cropped = Bitmap.createBitmap(bmp, (int)((deviceWidth / 4)*scaledX), (int)(((deviceHeight / 2) - (deviceWidth / 4))*scaledY), (int)((deviceWidth / 2)*scaledX), (int)((deviceWidth / 2)*scaledY));

        cropped =  Bitmap.createScaledBitmap(cropped,500,500,false);

        imageFile = fileHandler.getOutputMediaFile(FileHandler.MEDIA_TYPE_IMAGE);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(imageFile);
            cropped.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Pair<Bitmap,String> results = ImageUtils.newTec(cropped,this);
        if(results == null){
            Intent err = new Intent(this, ErrorActivity.class);
            err.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(err);
        }
        else {
            Bitmap bmp = results.m;
            String prediction = results.c;
            Intent i = new Intent(this, ShowResultsActivity.class);
            // Add extras to the bundle
            i.putExtra("Bitmap", bmp);
            i.putExtra("Prediction", prediction);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Bitmap retVal;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return retVal;
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {

    }
}