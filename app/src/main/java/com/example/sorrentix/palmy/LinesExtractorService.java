package com.example.sorrentix.palmy;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
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
        System.out.println("Sono entrato nel service");
        Notification noti = new NotificationCompat.Builder(this)
                .setContentTitle("Servizio Palmy")
                .setContentText("Service is active and is processing the image")
                .setSmallIcon(noti_icon)
                .build();

        Log.d(TAG, "" + noti.toString());

        startForeground(101, noti);
        System.out.println("Fatto start foreground");
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
        System.out.println("ottenuto i dati dal bundle");

        bmp = ImageResizer.checkAndRotate(imgPath, ImageResizer.decodeSampledBitmapFromFile(imgPath, imgWidth, imgHeight, null));//potrebbe fallire

        System.out.println("Fatto check and rotate");
        if (bmp == null) {
            rec.send(Activity.RESULT_CANCELED, new Bundle());
            return;
        }


        //System.out.println("mammt"+(deviceHeight/2-deviceWidth/2)+"  dw/2"+deviceWidth/2+" dh/2"+deviceHeight/2+" btn "+   btn.getHeight());
        // Bitmap cropped = Bitmap.createBitmap(bmp,1,(imgWidth + btnHeight)/2-imgHeight/2 ,deviceWidth-1,deviceWidth-1);
        //
        bmp = rotateImage(bmp, 90);
        System.out.println("DIM IMMAGINE:"+bmp.getWidth()+"x"+bmp.getHeight());
        System.out.println("REct x:"+(int)((deviceWidth / 4)*scaledX)+" rect y:"+(int)(((deviceHeight / 2) - (deviceWidth / 4))*scaledY)+" width:"+(int)((deviceWidth / 2)*scaledX)+"height"+(int)((deviceWidth / 2)*scaledY));

        Bitmap cropped;
        if(bmp.getWidth()>bmp.getHeight())
            cropped = Bitmap.createBitmap(bmp, (bmp.getHeight() / 4), (((bmp.getWidth()+(btnHeight/2)) / 2) - (bmp.getHeight() / 4)), (bmp.getWidth() / 2), (((bmp.getHeight()-btnHeight/2) / 2)-(btnHeight/2)));
        else
            cropped = Bitmap.createBitmap(bmp, (int)((deviceWidth / 4)*scaledX), (int)(((deviceHeight / 2) - (deviceWidth / 4))*scaledY), (int)((deviceWidth / 2)*scaledX), (int)((deviceWidth / 2)*scaledY));
      //  Bitmap resized =
System.out.println("cropped width"+cropped.getWidth()+" height"+ cropped.getHeight());
/*        if ( cropped.getWidth() < cropped.getHeight() ) {
            cropped =  Bitmap.createScaledBitmap(cropped,cropped.getWidth(),cropped.getWidth(),false);
        } else if( cropped.getWidth() > cropped.getHeight()) {
            cropped =  Bitmap.createScaledBitmap(cropped,cropped.getHeight(),cropped.getHeight(),false);
       }*/
        cropped =  Bitmap.createScaledBitmap(cropped,650,650,false);
        System.out.println("cropped after width"+cropped.getWidth()+" height"+ cropped.getHeight());

        System.out.println("Fatto crop + resize");
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

        //  MediaScannerConnection.scanFile(this, new String[]{fileHandler.getUriFromFile(imageFile).getPath()}, null, this);
        Uri fileUri = ImageUtils.mergeAndSave(cropped,this);
        imagePath = fileUri.getPath();
        System.out.println("Fatto merge and save");

        // To send a message to the Activity, create a pass a Bundle
        Bundle bundle = new Bundle();
        bundle.putString(Message.IMG_PATH, imagePath);
        // Here we call send passing a resultCode and the bundle of extras
        rec.send(Activity.RESULT_OK, bundle);
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