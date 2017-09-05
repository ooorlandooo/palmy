package com.example.sorrentix.palmy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

import org.opencv.core.MatOfInt4;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static java.lang.Math.abs;

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
        for (int i = 1; i < cannedImage.height()-1; i++) {
            for (int j = 1; j < cannedImage.width()-1; j++) {

             temp=  cannedImage.get(j, i);
                if (temp[0] >= 150) {
                    Point p1, p2;
                    p1 = new Point();
                    p2 = new Point();
                    if (i - 20 < 0) {
                        p1.x = 1;
                    } else {
                        p1.x = i - 20;
                    }
                    if (i + 20 > cannedImage.height()-1) {
                        p2.x = cannedImage.height()-2;
                    } else {
                        p2.x = i + 20;
                    }
                    if (j - 20 < 0) {
                        p1.y = 1;
                    } else {
                        p1.y = j - 20;
                    }
                    if (j + 20 > cannedImage.width()-1) {
                        p2.y = cannedImage.width()-2;
                    } else {
                        p2.y = j + 20;
                    }
                    Imgproc.rectangle(result, p1, p2, new Scalar(255, 255, 255), Core.FILLED);
                    cont+=(p2.x-p1.x)*(p2.y-p1.y);
                }
            }

        }
        cont = (cont/(result.width()*result.height()))*100;
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

    public static double[][] matToMatrix(Mat source) {
        if (source == null || source.empty()) {
            return null;
        }
        double[][] result = new double[source.height()][source.width()];

        for (int i=0; i<source.height(); i++) {
            for (int j=0; j<source.width();j++){
                result[i][j] = source.get(j,i)[0];
            }

        }
        return result;
    }

    public static void printMatrix(SparseArray<Double> source, Mat dimentionSource) {

        int i = 0,j = 0;

        for (int k = 0;k < source.size();k++) {

            System.out.print(" " + source.get(i*dimentionSource.width()+j) + " ");
            j++;
            if (k % dimentionSource.width() == 0 && k != 0) {
                i++;
                j=0;
                System.out.println("---------------------------");
                if( k>10*dimentionSource.width() ){break;}
            }

        }


    }

    public static void printOpenCVMatrix(Mat source) {

        for (int i=0;i<source.height();i++) {
            for (int j=0;j<source.width();j++) {
                System.out.print(" "+source.get(j,i)[0]+" ");
            }
            //if( i>10 ){break;}
            System.out.println("--------------------------------");
        }
    }



    public static Mat convertSparseArrayToMat(double[][] source, int height, int width){
        Mat result = new Mat(height,width,CvType.CV_8UC1,Scalar.all(0));
        for (int i=0;i<height;i++) {
            for (int j=0; j<width;j++) {
                result.put(j, i, source[i][j]);
            }
        }
        return result;
    }

    public static double[][] thinning(double[][] m, int height, int width){

        double[][] prev = new double[height][width];
        double[][] diff = new double[height][width];

        for(int i=0; i< height; i++){
            for (int j=0;j<width;j++){
                m[i][j] = m[i][j]/255;
                prev[i][j] = 0.0;

            }
        }

        int nonZeroDiff;
        do {
            m = thinningIteration(m, height,width, 0);
            m = thinningIteration(m,height,width, 1);

            nonZeroDiff = 0;
            for(int i=0; i< height; i++){
                for (int j=0;j<width;j++) {

                    diff[i][j] = Math.abs(m[i][j]) - Math.abs(prev[i][j]);
                    if (diff[i][j] != 0) {
                        nonZeroDiff++;
                    }
                    prev[i][j] = m[i][j];
                }
            }

        }
        while (nonZeroDiff > 0);

        for(int i=0; i< height; i++){
            for (int j=0;j<width;j++) {
                m[i][j]=m[i][j]*255;
            }
        }


        return m;
    }

    public static double[][] thinningIteration(double[][] m, int height, int width, int iter){
        double[][] marker = new double[height][width];
        double[][] result = new double[height][width];

        for(int i=0; i< height; i++){
            for (int j=0;j<width;j++) {
                marker[i][j] = 1.0;
            }
        }
        double[] p = new double[8];
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                if (m[i][j] != 0) {
                    p[0] = m[i-1][j];
                    p[1] = m[i-1][j+1];
                    p[2] = m[i][j+1];
                    p[3] = m[i+1][j+1];
                    p[4] = m[i+1][j];
                    p[5] = m[i+1][j-1];
                    p[6] = m[i][j-1];
                    p[7] =m[i-1][j-1];
                    int A = ((p[0] == 0 && p[1] == 1) ? 1 : 0) + ((p[1] == 0 && p[2] == 1) ? 1 : 0) +
                            ((p[2] == 0 && p[3] == 1) ? 1 : 0) + ((p[3] == 0 && p[4] == 1) ? 1 : 0) +
                            ((p[4] == 0 && p[5] == 1) ? 1 : 0) + ((p[5] == 0 && p[6] == 1) ? 1 : 0) +
                            ((p[6] == 0 && p[7] == 1) ? 1 : 0) + ((p[7] == 0 && p[0] == 1) ? 1 : 0);
                    double B = p[0] + p[1] + p[2] + p[3] + p[4] + p[5] + p[6] + p[7];
                    double m1 = (iter == 0 ? (p[0] * p[2] * p[4]) : (p[0] * p[2] * p[6]));
                    double m2 = (iter == 0 ? (p[2] * p[4] * p[6]) : (p[0] * p[4] * p[6]));

                    if (A == 1 && (B >= 2 && B <= 6) && m1 == 0 && m2 == 0 ) {
                        marker[i][j] = 0.0;
                    }

                }
            }
        }
        for(int i=0; i< height; i++) {
            for (int j = 0; j < width; j++) {
                if (m[i][j] == 1 && marker[i][j] == 1) {
                    m[i][j] = 1.0;
                } else {
                    m[i][j] = 0.0;
                }
            }
        }
        return m;
    }



    public static Uri mergeAndSave(Bitmap bmp, Bitmap bmp2, Context c){
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
        Imgproc.medianBlur(croppedImg,croppedImg,25);
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

            k = k - 0.5*(highThreshold+lowThreshold);
        }while(cont<=12);


        double[][] cannedImgMatrix = matToMatrix(p.m);
        double[][] thinned = thinning(cannedImgMatrix, cannedImg.height(),cannedImg.width());
        Mat thinnedImg = convertSparseArrayToMat(thinned,cannedImg.height(),cannedImg.width());
        MatOfInt4 hough = new MatOfInt4();

        Imgproc.HoughLinesP(thinnedImg, hough, 2, Math.PI/180, 15, 20, 20);
        //Imgproc.HoughLinesP(thinnedImg, hough, 1, Math.PI/180, 10, 30, 20);
        Imgproc.cvtColor(thinnedImg,thinnedImg,Imgproc.COLOR_GRAY2RGB);

        for (int i = 0; i < hough.rows(); i++) {

            double[] val = hough.get(i, 0);
            Imgproc.line(thinnedImg, new Point(val[0], val[1]), new Point(val[2], val[3]), new Scalar(255, 0, 0), 10);
        }

        bmpf = getResizedBitmap(bmpf,thinnedImg.width(),thinnedImg.height());
        Utils.matToBitmap(thinnedImg,bmpf);

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

