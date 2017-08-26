package com.example.sorrentix.palmy;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ALESSANDROSERRAPICA on 25/07/2017.
 */

public class ImageUtils {

    static {
        System.loadLibrary("opencv_java3");
    }

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

        Imgproc.blur(matsrc,matsrc,new Size(7,7));
        //Imgproc.GaussianBlur(matsrc,matsrc,new Size(5,5),0);
        //Imgproc.equalizeHist(matsrc,matsrc);
        //Imgproc.medianBlur(matsrc,matsrc,7);

        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,new Size(3,3));

        Imgproc.Canny(matsrc,matsrc,10,20,3,true);
        Imgproc.dilate(matsrc,matsrc,element);

        Imgproc.cvtColor(matsrc,matsrc,Imgproc.COLOR_GRAY2RGB);

        Imgproc.line(matsrc,new Point(0,0),new Point(100,100),new Scalar(255,0,0),5);

        Utils.matToBitmap(matsrc,bmpf);

        imageFile = fileHandler.getOutputMediaFile(FileHandler.MEDIA_TYPE_IMAGE);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(imageFile);
            bmpf.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileHandler.getUriFromFile(imageFile);

    }
}

