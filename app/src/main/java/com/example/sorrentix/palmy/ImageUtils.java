package com.example.sorrentix.palmy;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ALESSANDROSERRAPICA on 25/07/2017.
 */

public class ImageUtils {

    static {
        System.loadLibrary("opencv_java3");
    }

    private static final String TAG = "ImageUtils";
    private static FileHandler fileHandler = new FileHandler();
    private static File imageFile;


    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmOverlay;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
    public static Uri mergeAndSave(Bitmap bmp, Bitmap bmp2){
        bmp2= getResizedBitmap(bmp2,bmp.getWidth(),bmp.getHeight());
        Bitmap bmpf = overlay(bmp,bmp2);
        Mat matsrc = new Mat(bmpf.getHeight(),bmpf.getWidth(), CvType.CV_8UC3, new Scalar(4));
        Utils.bitmapToMat(bmpf,matsrc);
        Imgproc.cvtColor(matsrc,matsrc,Imgproc.COLOR_BGR2GRAY);

        //Ritaglio del palmo
        //TODO scegliere i punti in modo dinamico
        Point p1 = new Point(200,500),
              p2 = new Point(950,500),
              p3 = new Point(200,1250),
              p4 = new Point(950,1250);
        Rect rectCrop = new Rect(p1,p4); //new Rect((int)p1.x, (int)p1.y , (int)(p4.x-p1.x+1), (int)(p4.y-p1.y+1));
        Mat croppedImg= matsrc;//new Mat(matsrc,rectCrop);//submat(rectCrop);


        //Approccio cinese
        Imgproc.equalizeHist(croppedImg,croppedImg);
        Imgproc.medianBlur(croppedImg,croppedImg,15);


        int k = 0;
        MatOfDouble mean = new MatOfDouble(),
                    stdDev = new MatOfDouble();
        Core.meanStdDev(croppedImg,mean,stdDev);

        double highThreshold = mean.get(0,0)[0] + stdDev.get(0,0)[0] + k;
        double lowThreshold = mean.get(0,0)[0] - stdDev.get(0,0)[0];
        Log.e(TAG, "mergeAndSave: HT="+highThreshold+" - LT="+lowThreshold);

        Imgproc.Canny(croppedImg,croppedImg,highThreshold,lowThreshold);


        Imgproc.cvtColor(croppedImg,croppedImg,Imgproc.COLOR_GRAY2RGB);

     /*   Imgproc.line(croppedImg,p1,p2,new Scalar(255,0,0),5);
        Imgproc.line(croppedImg,p2,p4,new Scalar(255,0,0),5);
        Imgproc.line(croppedImg,p1,p3,new Scalar(255,0,0),5);
        Imgproc.line(croppedImg,p3,p4,new Scalar(255,0,0),5);
*/
     Imgproc.rectangle(croppedImg,p1,p4,new Scalar(255,0,0));

        Utils.matToBitmap(croppedImg,bmpf);

        imageFile = fileHandler.getOutputMediaFile(FileHandler.MEDIA_TYPE_IMAGE);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(imageFile);
            bmpf.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileHandler.getUriFromFile(imageFile);

    }
}

