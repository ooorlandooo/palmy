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

    public static Mat enlarging(Mat cannedImage){
        Mat result = new Mat(cannedImage.height(),cannedImage.width(),CvType.CV_8UC3, new Scalar(4));
        cannedImage.copyTo(result);
        Double myPixel;
        for (int i = 0; i < cannedImage.rows(); i++) {
            for (int j = 0; j < cannedImage.cols(); j++) {

                myPixel = cannedImage.get(i, j)[0];
                System.out.print("color value=" + myPixel);
                if (myPixel == 255) {
                    Point p1, p2;
                    p1 = new Point();
                    p2 = new Point();
                    if (i - 20 < 0) {
                        p1.x = 0;
                    } else {
                        p1.x = i - 20;
                    }
                    if (i + 20 > cannedImage.rows()) {
                        p2.x = cannedImage.rows();
                    } else {
                        p2.x = i + 20;
                    }
                    if (j - 20 < 0) {
                        p1.y = 0;
                    } else {
                        p1.y = j - 20;
                    }
                    if (j + 20 > cannedImage.cols()) {
                        p2.y = cannedImage.cols();
                    } else {
                        p2.y = j + 20;
                    }
                    Rect whiteRectangle = new Rect(p1, p2);
                    Imgproc.rectangle(result, p1, p2, new Scalar(255, 255, 255), Core.FILLED);
                }
            }

        }
        return result;
    }

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
        Mat croppedImg= new Mat(matsrc,rectCrop);//submat(rectCrop);


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


        croppedImg = enlarging(croppedImg);
        //Imgproc.cvtColor(croppedImg,croppedImg,Imgproc.COLOR_GRAY2RGB);

        bmpf = getResizedBitmap(bmpf,croppedImg.width(),croppedImg.height());
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

