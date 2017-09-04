package com.example.sorrentix.palmy;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.Settings;
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

    private static class Pair<T,S> {
        private T m;
        private S c;

        Pair(T mat, S cont) {
            m=mat;
            c=cont;
        }
    }


    public static Pair enlarging(Mat cannedImage){
        Mat result = new Mat(cannedImage.height(),cannedImage.width(),CvType.CV_8UC1);
        cannedImage.copyTo(result);
        double[] temp;
        int cont=0;
        for (int i = 0; i < cannedImage.height(); i++) {
            for (int j = 0; j < cannedImage.width(); j++) {

             temp=  cannedImage.get(j, i);
                if (temp[0] >= 150) {
                    Point p1, p2;
                    p1 = new Point();
                    p2 = new Point();
                    if (i - 20 < 0) {
                        p1.x = 0;
                    } else {
                        p1.x = i - 20;
                    }
                    if (i + 20 > cannedImage.height()-1) {
                        p2.x = cannedImage.height()-1;
                    } else {
                        p2.x = i + 20;
                    }
                    if (j - 20 < 0) {
                        p1.y = 0;
                    } else {
                        p1.y = j - 20;
                    }
                    if (j + 20 > cannedImage.width()-1) {
                        p2.y = cannedImage.width()-1;
                    } else {
                        p2.y = j + 20;
                    }
                    Imgproc.rectangle(result, p1, p2, new Scalar(255, 255, 255), Core.FILLED);
                    cont+=(p2.x-p1.x)*(p2.y-p1.y);
                }
            }

        }
       // System.out.println("Cont "+cont);
        cont = (cont/(result.width()*result.height()))*100;
        //System.out.println("Cont perc "+cont);
        return new Pair<Mat, Integer>(result,new Integer(cont));
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
    public static Mat thinning(Mat m){
        Core.divide(m,new Scalar(255),m);
       Mat prev = new Mat(m.height(),m.width(),CvType.CV_8UC1,Scalar.all(0));
        Mat diff = new Mat(m.height(),m.width(),CvType.CV_8UC1,Scalar.all(0));
        do {
            System.out.println("Chiamo iteration");
            thinningIteration(m, 0);
            thinningIteration(m, 1);
            Core.absdiff(m, prev, diff);
            m.copyTo(prev);
        }
        while (Core.countNonZero(diff) > 0);

        Core.multiply(m,new Scalar(255),m);
        return m;
    }

    public static Mat thinningIteration(Mat m, int iter){
        Mat marker = new Mat(m.height(),m.width(),CvType.CV_8UC1,Scalar.all(0));
        double[] p = new double[8];
        for (int i = 1; i < m.height()-1; i++) {
            for (int j = 1; j < m.width() - 1; j++) {
                p[0] = m.get(j, i - 1)[0];
                p[1] = m.get(j + 1, i - 1)[0];
                p[2] = m.get(j + 1, i)[0];
                p[3] = m.get(j + 1, i + 1)[0];
                p[4] = m.get(j, i + 1)[0];
                p[5] = m.get(j - 1, i + 1)[0];
                p[6] = m.get(j - 1, i)[0];
                p[7] = m.get(j - 1, i - 1)[0];
                int A = ((p[0] == 0 && p[1] == 1) ? 1 : 0) + ((p[1] == 0 && p[2] == 1) ? 1 : 0) +
                        ((p[2] == 0 && p[3] == 1) ? 1 : 0) + ((p[3] == 0 && p[4] == 1) ? 1 : 0) +
                        ((p[4] == 0 && p[5] == 1) ? 1 : 0) + ((p[5] == 0 && p[6] == 1) ? 1 : 0) +
                        ((p[6] == 0 && p[7] == 1) ? 1 : 0) + ((p[7] == 0 && p[0] == 1) ? 1 : 0);
                double B = p[0] + p[1] + p[2] + p[3] + p[4] + p[5] + p[6] + p[7];
                double m1 = iter == 0 ? (p[0] * p[2] * p[4]) : (p[0] * p[2] * p[4]);
                double m2 = iter == 0 ? (p[2] * p[4] * p[6]) : (p[0] * p[4] * p[6]);

                if (A == 1 && (B >= 2 && B <= 6) && m1 == 0 && m2 == 0)
                    marker.put(j, i, 1);
            }
        }

        System.out.println("torno al chiamante");
        Core.bitwise_not(marker,marker);
        Core.bitwise_and(m,marker,m);

        return m;
    }
    public static Uri mergeAndSave(Bitmap bmp, Bitmap bmp2){
        bmp2= getResizedBitmap(bmp2,bmp.getWidth(),bmp.getHeight());
        Bitmap bmpf = overlay(bmp,bmp2);
        Mat matsrc = new Mat(bmpf.getHeight(),bmpf.getWidth(), CvType.CV_8UC3, new Scalar(4));
        Utils.bitmapToMat(bmpf,matsrc);


        //Ritaglio del palmo
        //TODO scegliere i punti in modo dinamico
        Point p1 = new Point(200,500),
              p2 = new Point(950,500),
              p3 = new Point(200,1250),
              p4 = new Point(950,1250);
        Rect rectCrop = new Rect(p1,p4); //new Rect((int)p1.x, (int)p1.y , (int)(p4.x-p1.x+1), (int)(p4.y-p1.y+1));
        Mat croppedImg= new Mat(matsrc,rectCrop);//submat(rectCrop);

        Imgproc.cvtColor(croppedImg,croppedImg,Imgproc.COLOR_BGR2GRAY);
        //Approccio cinese
        Imgproc.equalizeHist(croppedImg,croppedImg);
        Imgproc.medianBlur(croppedImg,croppedImg,15);
        double cont = 0;
        double k = 0;
        Pair<Mat, Integer> p;
        MatOfDouble mean = new MatOfDouble(),
                stdDev = new MatOfDouble();
        Core.meanStdDev(croppedImg, mean, stdDev);
        double highThreshold = mean.get(0, 0)[0] + stdDev.get(0, 0)[0];
        double lowThreshold = mean.get(0, 0)[0] - stdDev.get(0, 0)[0];
        Mat cannedImg=new Mat(croppedImg.height(),croppedImg.width(),CvType.CV_8UC1);
        do {
            highThreshold+=k;
            Log.e(TAG, "mergeAndSave: HT=" + highThreshold + " - LT=" + lowThreshold);
            Imgproc.Canny(croppedImg, cannedImg, highThreshold, lowThreshold);
            p = enlarging(cannedImg);
            cont = ((double) Core.countNonZero(p.m) / (p.m.height() * p.m.width())) * 100;
            System.out.println("Conteggio pixel bianchi " + cont);

            k = k - 0.5*(highThreshold+lowThreshold);
        }while(cont<=12);
        cannedImg=thinning(cannedImg);
        //Imgproc.cvtColor(croppedImg,croppedImg,Imgproc.COLOR_GRAY2RGB);

        bmpf = getResizedBitmap(bmpf,cannedImg.width(),cannedImg.height());
        Utils.matToBitmap(cannedImg,bmpf);

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

