package com.example.sorrentix.palmy;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.example.sorrentix.palmy.util.ImageResizer;
import com.example.sorrentix.palmy.util.Message;

import static java.lang.Thread.sleep;

/**
 * Created by sorrentix on 09/09/2017.
 */

public class LinesExtractorService extends IntentService {

    private final static String TAG = "LinesExtractorService";

    private Bitmap bmp;
    private int imgWidth;
    private int imgHeight;
    private int btnHeight;
    private int deviceWidth;
    private String imgPath;


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
        int noti_icon = intent.getIntExtra(Message.NOTI_ICON,0);
        System.out.println("Sono entrato nel service");
        Notification noti = new NotificationCompat.Builder(this)
                .setContentTitle("Servizio Dalty")
                .setContentText("Service is active and is processing the image")
                .setSmallIcon(noti_icon)
                .build();

        Log.d(TAG,""+noti.toString());

        startForeground(101, noti);
        System.out.println("Fatto start foreground");
        if (intent == null) return;
        ResultReceiver rec = intent.getParcelableExtra(Message.RECEIVER_TAG);
        // Extract additional values from the bundle
        imgWidth = intent.getIntExtra(Message.PREVIEW_WIDTH,0);
        imgHeight = intent.getIntExtra(Message.PREVIEW_HEIGHT,0);
        btnHeight = intent.getIntExtra(Message.BTN_HEIGHT,0);
        deviceWidth = intent.getIntExtra(Message.DEVICE_WIDTH,0);
        imgPath = intent.getStringExtra(Message.IMG_PATH);

        try {//Per verificare il corretto funzionamento dell'icona
            sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("ottenuto i dati dal bundle");

        bmp = ImageResizer.checkAndRotate(imgPath,ImageResizer.decodeSampledBitmapFromFile(imgPath,imgWidth,imgHeight,null));//potrebbe fallire

        System.out.println("Fatto check and rotate");
        if(bmp==null) {
            rec.send(Activity.RESULT_CANCELED, new Bundle());
            return;
        }


        //System.out.println("mammt"+(deviceHeight/2-deviceWidth/2)+"  dw/2"+deviceWidth/2+" dh/2"+deviceHeight/2+" btn "+   btn.getHeight());
       // Bitmap cropped = Bitmap.createBitmap(bmp,1,(imgWidth + btnHeight)/2-imgHeight/2 ,deviceWidth-1,deviceWidth-1);
       // Bitmap resized = Bitmap.createScaledBitmap(cropped,500,500,false);

        System.out.println("Fatto crop + resize");


       // Uri fileUri = ImageUtils.mergeAndSave(resized,getApplicationContext());

        System.out.println("Fatto merge and save");

        // To send a message to the Activity, create a pass a Bundle
        Bundle bundle = new Bundle();
        bundle.putString(Message.IMG_PATH, imgPath);
        // Here we call send passing a resultCode and the bundle of extras
        rec.send(Activity.RESULT_OK, bundle);





    }
}
