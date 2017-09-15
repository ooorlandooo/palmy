package com.example.sorrentix.palmy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

import org.opencv.core.MatOfInt4;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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

        Pair(T m, S c) {
            this.m=m;
            this.c=c;
        }
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
              //  System.out.println("I= "+i+" J= "+j);
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



    public static Mat convertMatrixToMat(double[][] source, int height, int width){
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



    public static Uri mergeAndSave(Bitmap bmp, Context c){
        Mat croppedImg = new Mat(bmp.getHeight(),bmp.getWidth(), CvType.CV_8UC3, new Scalar(4));
        Mat filteredImg = new Mat(bmp.getHeight(),bmp.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bmp,croppedImg);

        Imgproc.cvtColor(croppedImg,croppedImg,Imgproc.COLOR_RGBA2GRAY);
        //Approccio cinese
        Imgproc.equalizeHist(croppedImg,croppedImg);
        //Imgproc.medianBlur(croppedImg,croppedImg,25);
        //Imgproc.blur(croppedImg,croppedImg,new Size(25,25));
        Imgproc.bilateralFilter(croppedImg,filteredImg,26,52,13);
        //salva(croppedImg, bmp, c);
        salva(filteredImg, bmp, c);
        double cont = 0;
        double k = 0;
        Pair<double[][], Integer> p;
        MatOfDouble mean = new MatOfDouble(),
                stdDev = new MatOfDouble();
        Core.meanStdDev(filteredImg, mean, stdDev);
        double highThreshold = (mean.get(0, 0)[0] + stdDev.get(0, 0)[0]);
        double lowThreshold = mean.get(0, 0)[0] - stdDev.get(0, 0)[0];
        System.out.println("DIM CROPPED: "+filteredImg.width()+"*"+filteredImg.height());
        double [][]matrixCanned = matToMatrix(filteredImg);
        double [][]matrixCannedImg;

        do {
            cont = 0;

            highThreshold += k;
            Log.e(TAG, "mergeAndSave: HT=" + highThreshold + " - LT=" + lowThreshold);

            matrixCannedImg = cannyEdgeDetector(matrixCanned, filteredImg.width(), filteredImg.height(), highThreshold, lowThreshold);
            //salva(convertMatrixToMat(matrixCannedImg, filteredImg.width(), filteredImg.height()), bmp, c);

            p = enlarging(matrixCannedImg, filteredImg.height(), filteredImg.width());
            //salva(convertMatrixToMat(p.m,filteredImg.width(), filteredImg.height()), bmp, c);


            for (int i = 0; i < filteredImg.height(); i++) {
                for (int j = 0; j < filteredImg.width(); j++) {
                    if (p.m[i][j] != 0) {
                        cont++;
                    }
                }
            }
            cont = (cont / (filteredImg.height() * filteredImg.width())) * 100;

            if (cont <= 20){
                k = k - 0.5 * (highThreshold + lowThreshold);
                k = -Math.abs(k);
            }else if(cont >= 27 || highThreshold<lowThreshold) {
                k = (k + 0.5 * (highThreshold + lowThreshold))/2;
                k=Math.abs(k);
            }

            //System.out.println("%punti bianchi: "+cont+ " k = mAmmt"+k);
        }while(cont<=20 || cont >=27);

        matrixCannedImg = pointIsolation(matrixCannedImg,filteredImg.height(),filteredImg.width());
        salva(convertMatrixToMat(matrixCannedImg,filteredImg.width(),  filteredImg.height()), bmp, c);

        Pair<double[][],Integer> enlarged = enlarging(matrixCannedImg, filteredImg.height(),filteredImg.width());
        double[][] thinned = thinning(enlarged.m, filteredImg.height(),filteredImg.width());
        Mat thinnedImg = convertMatrixToMat(thinned,filteredImg.height(),filteredImg.width());
        salva(thinnedImg, bmp, c);

        MatOfInt4 hough = new MatOfInt4();

        Imgproc.HoughLinesP(thinnedImg, hough, 2, Math.PI/180, 15, 20, 20);
        //Imgproc.HoughLinesP(thinnedImg, hough, 1, Math.PI/180, 10, 30, 20);
        Imgproc.cvtColor(thinnedImg,thinnedImg,Imgproc.COLOR_GRAY2RGB);

        Segment [] segments = new Segment[hough.rows()];
        double [] slopes = new double[hough.rows()];
        double [] locY = new double[hough.rows()];
        double [] locX = new double[hough.rows()];
        for (int i = 0; i < hough.rows(); i++) {
            double[] val = hough.get(i, 0);
            if (val[2]-val[0] == 0)
                slopes[i]=90;
            else
                slopes[i] = Math.toDegrees(Math.atan((val[3]-val[1])/(val[2]-val[0])));
            if (slopes[i]<0)
                slopes[i] = 360 + slopes[i];
            segments[i] = new Segment(val[0],val[1],val[2],val[3]);
            locY[i] = segments[i].puntoMedioY();
            locX[i] = segments[i].puntoMedioX();
            System.out.println("slopes[i]: "+slopes[i]+"  p1.x:"+val[0]+"  p1.y:"+val[1]+"  p2.x:"+val[2]+"  p2.y:"+val[3]+" atan:"+Math.atan((val[3]-val[1])/(val[2]-val[0]))+"locY[i]"+locY[i]+"locX[i]"+locX[i]);
        }
        return kMeanClustering(segments,slopes,locX,locY,bmp,thinnedImg);
/*
        bmp = getResizedBitmap(bmp,thinnedImg.width(),thinnedImg.height());
        Utils.matToBitmap(thinnedImg,bmp);

        imageFile = fileHandler.getOutputMediaFile(FileHandler.MEDIA_TYPE_IMAGE);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(imageFile);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileHandler.getUriFromFile(imageFile);
*/
    }

    private static String features2string(double [] features){
        return features[0]+"_"+features[1]+"_"+features[2];
    }


    private static Uri kMeanClustering(Segment[] segments, double[] slopes,double[] locX, double[] locY, Bitmap bmp, Mat thinnedImg){
        int k = 3;//MAIN LINES
        int numfeatures = 3;
        int passo = 0;
        boolean sameCentroidFlag = false;

        Map<String, Segment> map = new HashMap<>();//USED FOR PRINTING
        for (int j = 0; j < slopes.length; j++){
            double features [] = new double[numfeatures];
            features[0] = slopes[j];
            features[1] = locX[j];
            features[2] = locY[j];
            map.put(features2string(features),segments[j]);
        }

        //Random generator
        Random r = new Random();
        int rangeMin = 0, rangeMax = slopes.length-1;

        //Select random centroid
        double centroids [][] = new double[k][numfeatures];
        double prevCentroids [][] = new double[k][numfeatures];
        /*for (int i = 0; i < k; i++) {
            int randIndex = r.nextInt((rangeMax - rangeMin) + 1) + rangeMin;
            centroids[i][0] = slopes[randIndex];
            prevCentroids[i][0] = slopes[randIndex];
        }*/
        for (int i = 0; i < k; i++) {
            int randIndex = r.nextInt((rangeMax - rangeMin) + 1) + rangeMin;
            centroids[i][0] = slopes[randIndex];
            prevCentroids[i][0] = slopes[randIndex];
        }
        centroids[0][0] = 90;
        centroids[0][1] = thinnedImg.height()/5.0;
        centroids[0][2] = (thinnedImg.height()*4.0)/5.0;
        centroids[1][0] = 30;
        centroids[1][1] = thinnedImg.height()/2.0;
        centroids[1][2] = thinnedImg.height()/3.0;
        centroids[2][0] = 25;
        centroids[2][1] = thinnedImg.height()/2.0;//(thinnedImg.height()*4.0)/5.0;
        centroids[2][2] = thinnedImg.height()/6.0;

        prevCentroids[0][0] = 90;
        prevCentroids[0][1] = thinnedImg.height()/5.0;
        prevCentroids[0][2] = (thinnedImg.height()*4.0)/5.0;
        prevCentroids[1][0] = 30;
        prevCentroids[1][1] = thinnedImg.height()/2.0;
        prevCentroids[1][2] = thinnedImg.height()/3.0;
        prevCentroids[2][0] = 25;
        prevCentroids[2][1] = thinnedImg.height()/2.0;//(thinnedImg.height()*4.0)/5.0;
        prevCentroids[2][2] = thinnedImg.height()/6.0;

        double centroidiIniziali[][] = new double[k][numfeatures];
        centroidiIniziali[0][1] = thinnedImg.height()/5.0;
        centroidiIniziali[0][2] = (thinnedImg.height()*4.0)/5.0;
        centroidiIniziali[1][1] = thinnedImg.height()/2.0;
        centroidiIniziali[1][2] = thinnedImg.height()/3.0;
        centroidiIniziali[2][1] = thinnedImg.height()/2.0;//(thinnedImg.height()*4.0)/5.0;
        centroidiIniziali[2][2] = thinnedImg.height()/6.0;

        System.out.println("ALTEZZA IMG: "+ thinnedImg.height()+"thinnedimg/3: "+(thinnedImg.height()/3.0)+"thinnedimg/2: "+(thinnedImg.height()/2.0));
        System.out.println("CENTROIDI INIT: centroids[0][0]: "+ centroids[0][0]+"centroids[0][1]: "+ centroids[0][1]+"centroids[0][2]: "+ centroids[0][2]);
        System.out.println("CENTROIDI INIT: centroids[0][0]: "+ centroids[1][0]+"centroids[0][1]: "+ centroids[1][1]+"centroids[0][2]: "+ centroids[1][2]);
        System.out.println("CENTROIDI INIT: centroids[0][0]: "+ centroids[2][0]+"centroids[0][1]: "+ centroids[2][1]+"centroids[0][2]: "+ centroids[2][2]);
        //System.out.println("NUMERO RANDOM: "+(r.nextInt((rangeMax - rangeMin) + 1) + rangeMin));

        ArrayList<ArrayList<double[]>> clusters = new ArrayList<ArrayList<double[]>>();
        ArrayList<double[]> nodeList1 = new ArrayList<double[]>();
        clusters.add(nodeList1);
        ArrayList<double[]> nodeList2 = new ArrayList<double[]>();
        clusters.add(nodeList2);
        ArrayList<double[]> nodeList3 = new ArrayList<double[]>();
        clusters.add(nodeList3);

        double temp[];
        int i = 0;
        do {
            for (i = 0; i < slopes.length; i++) {
                double tempFeature [] = new double[numfeatures];
                tempFeature[0] = slopes[i];
                tempFeature[1] = locX[i];
                tempFeature[2] = locY[i];
                clusters.get(findNearestCentroid(centroids, slopes[i],locX[i], locY[i])).add(tempFeature);
            }
            i = 0;
/*            System.out.println("CENTROIDI PRIMA DEL RESET: prevCentroids[0][0]: "+ prevCentroids[0][0]+"prevCentroids[0][1]: "+ prevCentroids[0][1]+"prevCentroids[0][2]: "+ prevCentroids[0][2]);
            System.out.println("CENTROIDI PRIMA DEL RESET: prevCentroids[0][0]: "+ prevCentroids[1][0]+"prevCentroids[0][1]: "+ prevCentroids[1][1]+"prevCentroids[0][2]: "+ prevCentroids[1][2]);
            System.out.println("CENTROIDI PRIMA DEL RESET: prevCentroids[0][0]: "+ prevCentroids[2][0]+"prevCentroids[0][1]: "+ prevCentroids[2][1]+"prevCentroids[0][2]: "+ prevCentroids[2][2]);
            System.out.println("CENTROIDI PRIMA DEL RESET: centroids[0][0]: "+ centroids[0][0]+"centroids[0][1]: "+ centroids[0][1]+"centroids[0][2]: "+ centroids[0][2]);
            System.out.println("CENTROIDI PRIMA DEL RESET: centroids[0][0]: "+ centroids[1][0]+"centroids[0][1]: "+ centroids[1][1]+"centroids[0][2]: "+ centroids[1][2]);
            System.out.println("CENTROIDI PRIMA DEL RESET: centroids[0][0]: "+ centroids[2][0]+"centroids[0][1]: "+ centroids[2][1]+"centroids[0][2]: "+ centroids[2][2]);
*/            for(int j = 0; j < k; j++) {//RESET CENTROIDS
                centroids[j][0] = 0;
                centroids[j][1] = 0;
                centroids[j][2] = 0;
 //               System.out.println("RESET CENTROIDI : prevCentroids["+j+"][0]: "+ prevCentroids[j][0]+"prevCentroids["+j+"][1]: "+ prevCentroids[j][1]+"prevCentroids["+j+"][2]: "+ prevCentroids[j][2]);
 //               System.out.println("RESET CENTROIDI : centroids["+j+"][0]: "+ centroids[j][0]+"centroids["+j+"][1]: "+ centroids[j][1]+"centroids["+j+"][2]: "+ centroids[j][2]);
            }
            for (ArrayList<double[]> cluster : clusters) {
                for (int t = 0; t < cluster.size(); t++) {//NEW CENTROIDS AS MEAN OF NEW CLUSTERS
                    temp = cluster.remove(t);
                    centroids[i][0] += temp[0];
                    centroids[i][1] += temp[1];
                    centroids[i][2] += temp[2];
/*                    System.out.println("cluster.remove("+t+"): "+ temp[0]+" - "+ temp[1]+" - "+ temp[2]);
                    System.out.println("CALCOLO CENTROIDI - SOMMA: prevCentroids["+i+"][0]: "+ prevCentroids[i][0]+"prevCentroids["+i+"][1]: "+ prevCentroids[i][1]+"prevCentroids["+i+"][2]: "+ prevCentroids[i][2]);
                    System.out.println("CALCOLO CENTROIDI - SOMMA: centroids["+i+"][0]: "+ centroids[i][0]+"centroids["+i+"][1]: "+ centroids[i][1]+"centroids["+i+"][2]: "+ centroids[i][2]);
 */               }
                if(cluster.size()!=0) {
                    centroids[i][0] /= cluster.size();
                    centroids[i][1] /= cluster.size();
                    centroids[i][2] /= cluster.size();
/*                    System.out.println("CALCOLO CENTROIDI - DIVISIONE cluster.size(): "+ cluster.size());
                    System.out.println("CALCOLO CENTROIDI - DIVISIONE: prevCentroids["+i+"][0]: "+ prevCentroids[i][0]+"prevCentroids["+i+"][1]: "+ prevCentroids[i][1]+"prevCentroids["+i+"][2]: "+ prevCentroids[i][2]);
                    System.out.println("CALCOLO CENTROIDI - DIVISIONE: centroids["+i+"][0]: "+ centroids[i][0]+"centroids["+i+"][1]: "+ centroids[i][1]+"centroids["+i+"][2]: "+ centroids[i][2]);
*/                }
                i++;
            }
            sameCentroidFlag = true;
            for (i = 0; i < k; i++){
/*                    System.out.println("centroids["+i+"][0]: "+ centroids[i][0]+" prevcentroids["+i+"][0]"+ prevCentroids[i][0]);
                    System.out.println("centroids["+i+"][1]: "+ centroids[i][1]+" prevcentroids["+i+"][1]"+ prevCentroids[i][1]);
                    System.out.println("centroids["+i+"][2]: "+ centroids[i][2]+" prevcentroids["+i+"][2]"+ prevCentroids[i][2]);
 */                   if ((int)centroids[i][0] != (int)prevCentroids[i][0] || (int)centroids[i][1] != (int)prevCentroids[i][1] || (int)centroids[i][2] != (int)prevCentroids[i][2]) {
                        sameCentroidFlag = false;
                        prevCentroids[i][0] = centroids[i][0];
                        prevCentroids[i][1] = centroids[i][1];
                        prevCentroids[i][2] = centroids[i][2];
                    }
            }
            System.out.println("PASSO: "+passo + " FLAG: "+ sameCentroidFlag);
            passo++;
        }while (!sameCentroidFlag && passo < 1000);


        for (i = 0; i < slopes.length; i++) {//RECOMPUTE CLUSTERS
            double tempFeature [] = new double[numfeatures];
            tempFeature[0] = slopes[i];
            tempFeature[1] = locX[i];
            tempFeature[2] = locY[i];
            clusters.get(findNearestCentroid(centroids, slopes[i],locX[i], locY[i])).add(tempFeature);
        }

        int color = 0;
        for (ArrayList<double[]> cluster : clusters) {
            if (color == 0) {
                System.out.println("Rosso");
                for (double[] features : cluster)
                    Imgproc.line(thinnedImg, new Point(map.get(features2string(features)).getX1(), map.get(features2string(features)).getY1()), new Point(map.get(features2string(features)).getX2(), map.get(features2string(features)).getY2()), new Scalar(255, 0, 0), 10);
            }else if (color == 1){
                System.out.println("Verde");
                for (double[] features : cluster)
                    Imgproc.line(thinnedImg, new Point(map.get(features2string(features)).getX1(), map.get(features2string(features)).getY1()), new Point(map.get(features2string(features)).getX2(), map.get(features2string(features)).getY2()), new Scalar(0, 255, 0), 10);
            }else if (color == 2){
                System.out.println("Blu");
                for (double[] features : cluster)
                    Imgproc.line(thinnedImg, new Point(map.get(features2string(features)).getX1(), map.get(features2string(features)).getY1()), new Point(map.get(features2string(features)).getX2(), map.get(features2string(features)).getY2()), new Scalar(0, 0, 255), 10);
            }
            color++;
        }
        Imgproc.line(thinnedImg, new Point(centroidiIniziali[0][1],centroidiIniziali[0][2]),new Point(centroidiIniziali[0][1],centroidiIniziali[0][2]),new Scalar(255,0,0),20);
        Imgproc.line(thinnedImg, new Point(centroidiIniziali[1][1],centroidiIniziali[1][2]),new Point(centroidiIniziali[1][1],centroidiIniziali[1][2]),new Scalar(0,255,0),20);
        Imgproc.line(thinnedImg, new Point(centroidiIniziali[2][1],centroidiIniziali[2][2]),new Point(centroidiIniziali[2][1],centroidiIniziali[2][2]),new Scalar(0,0,255),20);

        Imgproc.line(thinnedImg, new Point(centroids[0][1],centroids[0][2]),new Point(centroids[0][1],centroids[0][2]),new Scalar(255,0,0),20);
        Imgproc.line(thinnedImg, new Point(centroids[1][1],centroids[1][2]),new Point(centroids[1][1],centroids[1][2]),new Scalar(0,255,0),20);
        Imgproc.line(thinnedImg, new Point(centroids[2][1],centroids[2][2]),new Point(centroids[2][1],centroids[2][2]),new Scalar(0,0,255),20);

        bmp = getResizedBitmap(bmp,thinnedImg.width(),thinnedImg.height());
        Utils.matToBitmap(thinnedImg,bmp);

        imageFile = fileHandler.getOutputMediaFile(FileHandler.MEDIA_TYPE_IMAGE);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(imageFile);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileHandler.getUriFromFile(imageFile);
    }



    private static int findNearestCentroid(double [][] centroids,double slope, double locX, double locY){
        double minVal = Double.MAX_VALUE, val;
        int minIndex=0;
        for (int i = 0; i < centroids.length; i++){
            val = euclideanDistance3D(centroids[i],slope,locX,locY);
            if(val < minVal){
                minVal = val;
                minIndex = i;
            }
        }
        return minIndex;
    }

    private static double euclideanDistance1D(double x, double y){
        return Math.abs(x-y);
    }

    private static double euclideanDistance2D(double[] centroid, double slope, double locY){
        return Math.sqrt(Math.pow(centroid[0]-slope,2)+Math.pow(centroid[1]-locY,2));
    }

    private static double euclideanDistance3D(double[] centroid, double slope,double locX, double locY){
        return Math.sqrt(Math.pow(centroid[0]-slope,2)+Math.pow(centroid[1]-locX,2)+Math.pow(centroid[2]-locY,2));
    }

    private static float gaussian(float x, double sigma) {
        return (float) Math.exp(-(x * x) / (2f * sigma * sigma));
    }

    private static double hypot(double x, double y) {
        return  Math.hypot(x, y);
    }


    public static double[][] cannyEdgeDetector(double[][] source,int width, int height, double highThreshold, double lowThreshold){
        double []result = new double[width*height];
        double [][]resultTwodim = new double[height][width];
        // statics
        double GAUSSIAN_CUT_OFF = 0.005f;
        double MAGNITUDE_SCALE = 100F;
        double MAGNITUDE_LIMIT = 1000F;
        int MAGNITUDE_MAX = (int) (MAGNITUDE_SCALE * MAGNITUDE_LIMIT);
        double kernelRadius = 2;
        int kernelWidth = 16;
        //generate the gaussian convolution masks
        double kernel[] = new double[kernelWidth];
        double diffKernel[] = new double[kernelWidth];
        double[] xConv = new double[width*height];
        double[] yConv = new double[width*height];
        double [] xGradient = new double[width*height];
        double [] yGradient = new double[width*height];
        int[] magnitude = new int[width*height];

        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++){
                result[i*width+j] = source[i][j];
            }
        }

        int kwidth;

        for (kwidth = 0; kwidth < kernelWidth; kwidth++) {

            float g1 = gaussian(kwidth, kernelRadius);
            if (g1 <= GAUSSIAN_CUT_OFF && kwidth >= 2) break;
            float g2 = gaussian(kwidth - 0.5f, kernelRadius);
            float g3 = gaussian(kwidth + 0.5f, kernelRadius);
            kernel[kwidth] = (g1 + g2 + g3) / 3f / (2f * (float) Math.PI * kernelRadius * kernelRadius);
            diffKernel[kwidth] = g3 - g2;
        }

        int initX = kwidth - 1;
        int maxX = width - (kwidth - 1);
        int initY = width * (kwidth - 1);
        int maxY = width * (height - (kwidth - 1));

        //perform convolution in x and y directions
        for (int x = initX; x < maxX; x++) {
            for (int y = initY; y < maxY; y += width) {
                int index = x + y;
                double sumX = result[index] * kernel[0];
                double sumY = sumX;
                int xOffset = 1;
                int yOffset = width;
                for(; xOffset < kwidth ;) {
                    sumY += kernel[xOffset] * (result[index - yOffset] + result[index + yOffset]);
                    sumX += kernel[xOffset] * (result[index - xOffset] + result[index + xOffset]);
                    yOffset += width;
                    xOffset++;
                }

                yConv[index] = sumY;
                xConv[index] = sumX;
            }

        }

        for (int x = initX; x < maxX; x++) {
            for (int y = initY; y < maxY; y += width) {
                float sum = 0f;
                int index = x + y;
                for (int i = 1; i < kwidth; i++)
                    sum += diffKernel[i] * (yConv[index - i] - yConv[index + i]);

                xGradient[index] = sum;
            }

        }

        for (int x = kwidth; x < width - kwidth; x++) {
            for (int y = initY; y < maxY; y += width) {
                float sum = 0.0f;
                int index = x + y;
                int yOffset = width;
                for (int i = 1; i < kwidth; i++) {
                    sum += diffKernel[i] * (xConv[index - yOffset] - xConv[index + yOffset]);
                    yOffset += width;
                }

                yGradient[index] = sum;
            }

        }

        initX = kwidth;
        maxX = width - kwidth;
        initY = width * kwidth;
        maxY = width * (height - kwidth);
        for (int x = initX; x < maxX; x++) {
            for (int y = initY; y < maxY; y += width) {
                int index = x + y;
                int indexN = index - width;
                int indexS = index + width;
                int indexW = index - 1;
                int indexE = index + 1;
                int indexNW = indexN - 1;
                int indexNE = indexN + 1;
                int indexSW = indexS - 1;
                int indexSE = indexS + 1;

                double xGrad = xGradient[index];
                double yGrad = yGradient[index];
                double gradMag = hypot(xGrad, yGrad);

                //perform non-maximal supression
                double nMag = hypot(xGradient[indexN], yGradient[indexN]);
                double sMag = hypot(xGradient[indexS], yGradient[indexS]);
                double wMag = hypot(xGradient[indexW], yGradient[indexW]);
                double eMag = hypot(xGradient[indexE], yGradient[indexE]);
                double neMag = hypot(xGradient[indexNE], yGradient[indexNE]);
                double seMag = hypot(xGradient[indexSE], yGradient[indexSE]);
                double swMag = hypot(xGradient[indexSW], yGradient[indexSW]);
                double nwMag = hypot(xGradient[indexNW], yGradient[indexNW]);
                double tmp;
				/*
				 * An explanation of what's happening here, for those who want
				 * to understand the source: This performs the "non-maximal
				 * supression" phase of the Canny edge detection in which we
				 * need to compare the gradient magnitude to that in the
				 * direction of the gradient; only if the value is a local
				 * maximum do we consider the point as an edge candidate.
				 *
				 * We need to break the comparison into a number of different
				 * cases depending on the gradient direction so that the
				 * appropriate values can be used. To avoid computing the
				 * gradient direction, we use two simple comparisons: first we
				 * check that the partial derivatives have the same sign (1)
				 * and then we check which is larger (2). As a consequence, we
				 * have reduced the problem to one of four identical cases that
				 * each test the central gradient magnitude against the values at
				 * two points with 'identical support'; what this means is that
				 * the geometry required to accurately interpolate the magnitude
				 * of gradient function at those points has an identical
				 * geometry (upto right-angled-rotation/reflection).
				 *
				 * When comparing the central gradient to the two interpolated
				 * values, we avoid performing any divisions by multiplying both
				 * sides of each inequality by the greater of the two partial
				 * derivatives. The common comparand is stored in a temporary
				 * variable (3) and reused in the mirror case (4).
				 *
				 */
                if (xGrad * yGrad <= (float) 0 /*(1)*/
                        ? Math.abs(xGrad) >= Math.abs(yGrad) /*(2)*/
                        ? (tmp = Math.abs(xGrad * gradMag)) >= Math.abs(yGrad * neMag - (xGrad + yGrad) * eMag) /*(3)*/
                        && tmp > Math.abs(yGrad * swMag - (xGrad + yGrad) * wMag) /*(4)*/
                        : (tmp = Math.abs(yGrad * gradMag)) >= Math.abs(xGrad * neMag - (yGrad + xGrad) * nMag) /*(3)*/
                        && tmp > Math.abs(xGrad * swMag - (yGrad + xGrad) * sMag) /*(4)*/
                        : Math.abs(xGrad) >= Math.abs(yGrad) /*(2)*/
                        ? (tmp = Math.abs(xGrad * gradMag)) >= Math.abs(yGrad * seMag + (xGrad - yGrad) * eMag) /*(3)*/
                        && tmp > Math.abs(yGrad * nwMag + (xGrad - yGrad) * wMag) /*(4)*/
                        : (tmp = Math.abs(yGrad * gradMag)) >= Math.abs(xGrad * seMag + (yGrad - xGrad) * sMag) /*(3)*/
                        && tmp > Math.abs(xGrad * nwMag + (yGrad - xGrad) * nMag) /*(4)*/
                        ) {
                    magnitude[index] = gradMag >= MAGNITUDE_LIMIT ? MAGNITUDE_MAX : (int) (MAGNITUDE_SCALE * gradMag);
                    //NOTE: The orientation of the edge is not employed by this
                    //implementation. It is a simple matter to compute it at
                    //this point as: Math.atan2(yGrad, xGrad);
                } else {
                    magnitude[index] = 0;
                }
            }
        }

        Arrays.fill(result, 0);

        int offset = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //////6* orla
                if (result[offset] == 0 && magnitude[offset] >= highThreshold) {
                    follow(result, x, y, offset,width, height, (int)lowThreshold, magnitude);
                }
                offset++;
            }
        }

        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++){
                resultTwodim[i][j]=result[i*width+j];
            }
        }

        return resultTwodim;
    }

    private static void follow(double[] source, int x1, int y1, int i1,int width,int height, int threshold, int[]magnitude) {
        int x0 = x1 == 0 ? x1 : x1 - 1;
        int x2 = x1 == width - 1 ? x1 : x1 + 1;
        int y0 = y1 == 0 ? y1 : y1 - 1;
        int y2 = y1 == height -1 ? y1 : y1 + 1;

        source[i1] = magnitude[i1];
        for (int x = x0; x <= x2; x++) {
            for (int y = y0; y <= y2; y++) {
                int i2 = x + y * width;
                if ((y != y1 || x != x1)
                        && source[i2] == 0
                        && magnitude[i2] >= threshold) {
                    follow(source, x, y, i2, width, height,threshold,magnitude);
                    return;
                }
            }
        }
    }

    public static double[][] pointIsolation(double [][] m, int height, int width){

        //Indici per identificare le aree da controllare
        int x1=0;
        int y1=0;
        int x2=0;
        int y2=0;


        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {

                if(m[i][j]>=150.0) {
                    if(i-20 < 0)
                        x1=0;
                    else x1=i-20;
                    if(j-20 < 0)
                        y1=0;
                    else y1 = j-20;
                    if(i+20 > height -1)
                        x2=height-1;
                    else x2=i+20;
                    if(j+20 > width -1)
                        y2 = width-1;
                    else y2 = j+20;
                    if(!checkNeighbors(m,x1,y1,x2,y2))
                        m[i][j]=0.0;
                }
            }
        }

        return m;
    }

    public static boolean checkNeighbors(double [][]m,int x1,int y1, int x2, int y2) {

        int cont = 0;
        for (int i = x1; i < x2 - 1; i++) {
            for (int j = y1; j < y2 - 1; j++) {
                if (m[i][j] >= 150.0)
                    cont++;
                if (cont == 20)
                    return true;
            }
        }
        return false;
    }

    public static Pair enlarging(double[][] cannedImage,int height, int width){

        double[][] result = new double[height][width];
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                result[i][j] = cannedImage[i][j];
            }
        }
        double temp;
        int cont=0;
        for (int i = 1; i < height-1; i++) {
            for (int j = 1; j < width-1; j++) {

                temp=  cannedImage[i][j];
                if (temp >= 150) {
                    Point p1, p2;
                    p1 = new Point();
                    p2 = new Point();
                    if (i - 20 < 0) {
                        p1.x = 1;
                    } else {
                        p1.x = i - 20;
                    }
                    if (i + 20 > height) {
                        p2.x = height-2;
                    } else {
                        p2.x = i + 20;
                    }
                    if (j - 20 < 0) {
                        p1.y = 1;
                    } else {
                        p1.y = j - 20;
                    }
                    if (j + 20 > width-1) {
                        p2.y = width-2;
                    } else {
                        p2.y = j + 20;
                    }
                    whiteRectangle(result, p1, p2);
                    cont+=(p2.x-p1.x)*(p2.y-p1.y);
                }
            }

        }
        cont = (cont/(width*height))*100;
        return new Pair<double[][], Integer>(result,new Integer(cont));
    }

    public static void whiteRectangle(double[][] m,Point p1, Point p2){
        for (int i = (int)p1.x; i < p2.x; i++) {
            for (int j =(int) p1.y; j < p2.y; j++) {
                m[i][j] = 255.0;
            }
        }
    }

    public static void salva(Mat img,Bitmap bmp,Context c){

        Bitmap bmp2 = Bitmap.createBitmap(bmp);
        bmp2 = getResizedBitmap(bmp2,img.width(),img.height());
        Utils.matToBitmap(img,bmp2);

        imageFile = fileHandler.getOutputMediaFile(FileHandler.MEDIA_TYPE_IMAGE);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(imageFile);
            bmp2.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaScannerConnection.scanFile(c, new String[]{fileHandler.getUriFromFile(imageFile).getPath()}, null, (MediaScannerConnection.OnScanCompletedListener)c);

    }

}

