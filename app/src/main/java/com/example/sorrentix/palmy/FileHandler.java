package com.example.sorrentix.palmy;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sorrentix on 26/08/2017.
 */

public class FileHandler {

    private static final String TAG = "FileHandler";
    public static final int MEDIA_TYPE_IMAGE = 1;

    //private Activity activityCaller;

    public FileHandler(){
        //activityCaller=activity;
    }

    /** Create a file Uri for saving an image or video */
    public Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    public File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        if (!isExternalStorageWritable()){
            Log.d(TAG, "SD card not present or read only");
            //Toast.makeText(activityCaller, "SD card not present or read only", Toast.LENGTH_LONG).show();
            return null;
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Palmy");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".png");
            mediaFile.setReadable(true,false);
        } else {
            return null;
        }

        return mediaFile;
    }

    public Uri getUriFromFile(File file){
        return Uri.fromFile(file);
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

}
