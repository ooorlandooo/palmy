package com.example.sorrentix.palmy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import com.example.sorrentix.palmy.util.Message;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import static org.opencv.imgproc.Imgproc.CC_STAT_AREA;

/**
 * Created by ALESSANDROSERRAPICA on 25/07/2017.
 */

public class ImageUtils {

    static {
        System.loadLibrary("opencv_java3");
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

    public static Rgb[][] matToRgbMatrix(Mat source) {
        if (source == null || source.empty()) {
            return null;
        }

        Rgb[][] result = new Rgb[source.height()][source.width()];
        double [] temp = new double[3];
        for (int i=0; i<source.height(); i++) {
            for (int j=0; j<source.width();j++){
                temp=source.get(j,i);
                result[i][j] = new Rgb(temp[0],temp[1],temp[2]);
            }

        }
        return result;
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

    public static Pair<ArrayList<Point>,Mat> leftHeartLineDetection(Rgb[][] mat,Mat thinnedImg, int height, int width){


        ArrayList<Point> arr = new ArrayList<Point>();
        int i=0,j=0;
        boolean flag=false;
        //Scelta candidato
         for(int k=width-10;k>=0;k--){
            for(int z=0; z<height/2; z++){

             //   System.out.println("Provo indici: "+ z+" "+k);
                if((int)mat[k][z].getRed()==255 && (int)mat[k][z].getGreen()==0 && (int)mat[k][z].getBlue()==0){
                    i=k;
                    j=z;

                    break;
                }
                else if ((int)mat[k][z].getRed()==255 && (int)mat[k][z].getGreen()==255 && (int)mat[k][z].getBlue()==255){
                    mat[k][z].setBlue(0);
                    mat[k][z].setRed(0);
                    mat[k][z].setGreen(0);
                }
            }
            if(i!=0 && j!=0)
                break;
         }

        arr.add(new Point(i,j));
        mat[i][j].setBlue(0);  mat[i][j].setGreen(0);  mat[i][j].setRed(0);
        Imgproc.line(thinnedImg, new Point(i,j), new Point(i,j), new Scalar(0, 0, 0), 30);
        flag=false;
        boolean stop=false;
        //TO DO: MODO PIU' EFFICIENTE?
        while(j>0 && !stop) {
            flag = false;
            for (int k = i - 5; k < i + 2; k++) {
                for (int z = j - 5; z < j + 2; z++) {
                    if (i >= 5 && i<width-2 && j >= 5 && j < width -2 && (((int) mat[k][z].getRed() == 255 && (int) mat[k][z].getGreen() == 0 && (int) mat[k][z].getBlue() == 0) || ((int) mat[k][z].getRed() == 255 && (int) mat[k][z].getGreen() == 255 && (int) mat[k][z].getBlue() == 255))) {

                        mat[k][z].setBlue(0);
                        mat[k][z].setRed(0);
                        mat[k][z].setGreen(0);

                        Imgproc.line(thinnedImg, new Point(k, z), new Point(k, z), new Scalar(0, 0, 0), 30);

                            Point p = new Point(k, z);
                            arr.add(p);
                            i = k;
                            j = z;
                            flag = true;
                            break;

                    }
                }
               if(flag==true)
                   break;
            }
            if (!flag)
                stop = true;
        }
        Pair<ArrayList<Point>,Mat> p = new Pair<ArrayList<Point>,Mat>(arr,thinnedImg);
        return p;


    }

    public static Pair<ArrayList<Point>,Mat> leftHeadLineDetection(Rgb[][] mat,Mat thinnedImg, int height, int width){


        ArrayList<Point> arr = new ArrayList<Point>();
        int i=0,j=0;
        boolean flag=false;
        //Scelta candidato
        for(int k=0;k<width-1;k++){
            for(int z=0; z<0.45*height; z++){
                if((int)mat[k][z].getRed()==255 && (int)mat[k][z].getGreen()==0 && (int)mat[k][z].getBlue()==0){
                    i=k;
                    j=z;
                    break;
                }
                else if ((int)mat[k][z].getRed()==255 && (int)mat[k][z].getGreen()==255 && (int)mat[k][z].getBlue()==255){
                    mat[k][z].setBlue(0);
                    mat[k][z].setRed(0);
                    mat[k][z].setGreen(0);
                }
            }
            if(i!=0 && j!=0)
                break;
        }

        arr.add(new Point(i,j));
        mat[i][j].setBlue(0);  mat[i][j].setGreen(0);  mat[i][j].setRed(0);
        Imgproc.line(thinnedImg, new Point(i,j), new Point(i,j), new Scalar(0, 0, 0), 30);
        flag=false;
        boolean stop=false;
        while(i<width && !stop) {
            flag=false;
            for (int k=i+1; k<=i+5; k++) {
                for (int z = j + 5; z > j - 2; z--) {
                    if (i < width-5 && j <width-5 && j > 2 && (((int) mat[k][z].getRed() > 0 && (int) mat[k][z].getGreen() > 0 && (int) mat[k][z].getBlue() > 0) || ((int) mat[k][z].getRed() > 0 && (int) mat[k][z].getGreen() == 0 && (int) mat[k][z].getBlue() == 0))) {

                       Imgproc.line(thinnedImg, new Point(k, z), new Point(k, z), new Scalar(0, 0, 0), 30);

                            Point p = new Point(k, z);
                            arr.add(p);
                            i = k;
                            j = z;
                            flag = true;
                            break;
                        }
                    }

                if(flag)
                    break;
            }
            if(!flag)
                stop=true;
        }
        Pair<ArrayList<Point>,Mat> p = new Pair<ArrayList<Point>,Mat>(arr,thinnedImg);
        return p;


    }

    public static Pair<ArrayList<Point>,Mat> leftLifeLineDetection(Rgb[][] mat,Mat thinnedImg, int height, int width){


        ArrayList<Point> arr = new ArrayList<Point>();
        int i=0,j=0;
        boolean flag=false;
        //Scelta candidato
        for(int z=height-1; z>=(int)(0.4*height); z--){
            for(int k=(int)(0.5*width);k>=(int)(0.3*width);k--){

                if((int)mat[k][z].getRed()==255 && (int)mat[k][z].getGreen()==0 && (int)mat[k][z].getBlue()==0){
                    i=k;
                    j=z;
                    System.out.println("PRESO TRA 0.5 e 0.3");
                    break;
                }
                else if ((int)mat[k][z].getRed()==255 && (int)mat[k][z].getGreen()==255 && (int)mat[k][z].getBlue()==255){
                    mat[k][z].setBlue(0);
                    mat[k][z].setRed(0);
                    mat[k][z].setGreen(0);
                }
            }
            if(i!=0 && j!=0) {
                flag=true;
                break;
            }
        }
        if(!flag){
            for(int z=height-1; z>=(int)(0.4*height); z--){
                for(int k=(int)(0.6*width);k>=(int)(0.5*width);k--){

                    if((int)mat[k][z].getRed()==255 && (int)mat[k][z].getGreen()==0 && (int)mat[k][z].getBlue()==0){
                        i=k;
                        j=z;

                        System.out.println("PRESO TRA 0.6 e 0.5");
                        break;
                    }
                    else if ((int)mat[k][z].getRed()==255 && (int)mat[k][z].getGreen()==255 && (int)mat[k][z].getBlue()==255){
                        mat[k][z].setBlue(0);
                        mat[k][z].setRed(0);
                        mat[k][z].setGreen(0);
                    }
                }
                if(i!=0 && j!=0) {
                    flag=true;
                    break;
                }
            }
        }
        arr.add(new Point(i,j));
        mat[i][j].setBlue(0);  mat[i][j].setGreen(0);  mat[i][j].setRed(0);
        Imgproc.line(thinnedImg, new Point(i,j), new Point(i,j), new Scalar(0, 0, 0), 30);
        flag=false;
        boolean stop=false;
        while(j>0 && !stop) {
            flag=false;
            for (int k=i-2; k<i+3; k++) {
                for (int z = j - 10; z < j + 2; z++) {
                    if (i >= 2 && i<width-3 && z< width -2 && j >= 10 && (((int) mat[k][z].getRed() == 255 && (int) mat[k][z].getGreen() == 0 && (int) mat[k][z].getBlue() == 0) || ((int) mat[k][z].getRed() == 255 && (int) mat[k][z].getGreen() == 255 && (int) mat[k][z].getBlue() == 255))) {

                        mat[k][z].setBlue(0);
                        mat[k][z].setRed(0);
                        mat[k][z].setGreen(0);

                        Imgproc.line(thinnedImg, new Point(k, z), new Point(k, z), new Scalar(0, 0, 0), 30);

                        Point p = new Point(k, z);
                        arr.add(p);
                        i = k;
                        j = z;
                        flag = true;
                        break;
                    }
                }
                if(flag)
                    break;
            }
            if(!flag)
                stop=true;
        }

        Pair<ArrayList<Point>,Mat> p = new Pair<ArrayList<Point>,Mat>(arr,thinnedImg);
        return p;


    }

    private static double getLineLength(ArrayList<Point> arr){
        double cont =0;
        for(int i=0; i<arr.size()-1; i++){
            cont = cont + Math.sqrt(Math.abs((arr.get(i).x-arr.get(i+1).x)+(arr.get(i).y-arr.get(i+1).y)));
        }
        return cont;
    }


    public static double[][] contoursCleaning(double[][]m , int height, int width){
        for (int i = 0; i <= height - 1; i++) {
            for (int j = 0; j <= width - 1; j++) {

                if (i == 0 || j == 0 || i == height - 1 || j == width - 1)
                    m[i][j] = 0;

            }
        }
        return m;
    }



    public static Pair enlarging(double[][] cannedImage, int height, int width,int kernel){

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
                    if (i - kernel < 0) {
                        p1.x = 1;
                    } else {
                        p1.x = i - kernel;
                    }
                    if (i + kernel > height) {
                        p2.x = height-2;
                    } else {
                        p2.x = i + kernel;
                    }
                    if (j - kernel < 0) {
                        p1.y = 1;
                    } else {
                        p1.y = j - kernel;
                    }
                    if (j + kernel > width-1) {
                        p2.y = width-2;
                    } else {
                        p2.y = j + kernel;
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

    private static Cluster[]  singleLinkClustering(Segment[] segments, double dimention) {
        int clustersNumberGoal = 3;
        int actualClustersNumber = segments.length;
        Cluster [] clusters = new Cluster[segments.length];
        double [][] proximityMatrix = new double[segments.length][segments.length];

        System.out.println("segments number:"+segments.length);

        for (int i = 0; i < segments.length; i++) {
            clusters[i] = new Cluster(segments[i],segments.length,i);
        }
        for (int i = 0; i < segments.length; i++) {
            for (int j = 0; j <= i; j++) {
                if (i==j){
                    proximityMatrix[i][j] = 0;
                } else {
                    proximityMatrix[i][j] = clusters[i].initDistance(clusters[j]);
                    proximityMatrix[j][i] = proximityMatrix[i][j];
                }
            }
        }


        while(clustersNumberGoal < actualClustersNumber){
            int firstNearestClusterIndex = -1, secondNearestClusterIndex = -1;
            double currentLowerValue = dimention*dimention;

            //cerco i due cluster piu vicini
            for (int i = 0; i < segments.length; i++) {
                for (int j = 0; j <= i; j++) {
                    if (proximityMatrix[i][j] > 0){
                        if      (currentLowerValue > proximityMatrix[i][j] ||
                                (currentLowerValue == proximityMatrix[i][j] && clusters[firstNearestClusterIndex].clusterElements + clusters[secondNearestClusterIndex].clusterElements>(clusters[i].clusterElements + clusters[j].clusterElements))){
                            firstNearestClusterIndex = i;
                            secondNearestClusterIndex = j;
                            currentLowerValue = proximityMatrix[i][j];
                        }
                    }
                }
            }

            //faccio il merge dei cluster prescelti
            clusters[firstNearestClusterIndex].addClusterSegments(clusters[secondNearestClusterIndex]);

            //faccio l'update della matrice di prossimita
            for (int i = 0; i < segments.length; i++) {

                if (i !=firstNearestClusterIndex) {
                    proximityMatrix[firstNearestClusterIndex][i] = Math.min(proximityMatrix[firstNearestClusterIndex][i],proximityMatrix[secondNearestClusterIndex][i]);
                    proximityMatrix[i][firstNearestClusterIndex] = proximityMatrix[firstNearestClusterIndex][i];

                    proximityMatrix[secondNearestClusterIndex][i]  = 0;
                    proximityMatrix [i][secondNearestClusterIndex] = 0;
                }
            }
            actualClustersNumber--;

        }

        for (int k = 0; k < clusters.length; k++ ){
            int firstNearestClusterIndex = k, secondNearestClusterIndex = -1;
            double currentLowerValue = dimention*dimention;
            if ( clusters[k].clusterElements < 3 && clusters[k].clusterElements > 0 ){
                //cerco i due cluster piu vicini
                for (int j = 0; j <= k; j++) {
                    if (proximityMatrix[k][j] > 0){
                        if (currentLowerValue > proximityMatrix[k][j] ){
                            firstNearestClusterIndex = k;
                            secondNearestClusterIndex = j;
                            currentLowerValue = proximityMatrix[k][j];
                        }
                    }
                }
                //faccio il merge dei cluster prescelti
                if (!(firstNearestClusterIndex ==-1 || secondNearestClusterIndex == -1)) {
                    clusters[firstNearestClusterIndex].addClusterSegments(clusters[secondNearestClusterIndex]);
                    for (int i = 0; i < segments.length; i++) {

                        if (i !=firstNearestClusterIndex) {
                            proximityMatrix[firstNearestClusterIndex][i] = Math.min(proximityMatrix[firstNearestClusterIndex][i],proximityMatrix[secondNearestClusterIndex][i]);
                            proximityMatrix[i][firstNearestClusterIndex] = proximityMatrix[firstNearestClusterIndex][i];

                            proximityMatrix[secondNearestClusterIndex][i]  = 0;
                            proximityMatrix [i][secondNearestClusterIndex] = 0;
                        }
                    }
                    clustersNumberGoal--;
                }
            }
        }


       Cluster []result = new Cluster[clustersNumberGoal];
        int index = 0;
       for (int i = 0; i< clusters.length;i++){
           if (clusters[i].clusterElements > 0 ){
               result[index] = clusters[i];
               index++;
           }
       }


       return result;
    }


    public static Pair<Bitmap,String> newTec(Bitmap bmp, Context c){

        Bitmap bmp2 = Bitmap.createBitmap(bmp);

        Mat croppedImg = new Mat(bmp.getHeight(),bmp.getWidth(), CvType.CV_8UC3);
        Mat filteredImg = new Mat(bmp.getHeight(),bmp.getWidth(), CvType.CV_8UC1);
        Mat whiteImg = new Mat(bmp.getHeight(),bmp.getWidth(), CvType.CV_8UC1,new Scalar(255));
        Mat negative =  new Mat(bmp.getHeight(),bmp.getWidth(), CvType.CV_8UC1);
        Mat topHatted =  new Mat(bmp.getHeight(),bmp.getWidth(), CvType.CV_8UC1);
        Mat topHattedEqualized =  new Mat(bmp.getHeight(),bmp.getWidth(), CvType.CV_8UC1);
        Mat binary =  new Mat(bmp.getHeight(),bmp.getWidth(), CvType.CV_8UC1);
        Mat labeled =  new Mat(bmp.getHeight(),bmp.getWidth(), CvType.CV_8UC1);
        Mat finalImg =  new Mat(bmp.getHeight(),bmp.getWidth(), CvType.CV_8UC3);

        Utils.bitmapToMat(bmp,croppedImg);

        Imgproc.cvtColor(croppedImg,croppedImg,Imgproc.COLOR_RGBA2GRAY);

//PROCESSING PHASE
        //STEP 1 todo check equalize method
        Imgproc.equalizeHist(croppedImg,croppedImg);
        //matriciDaSalvare.add(croppedImg);
        Utils.matToBitmap(croppedImg,bmp);
        saveImageToExternalStorage(bmp);
        //STEP 2 todo check filter type (suggested gausssian 5x5 + median 3x3)
        //Imgproc.bilateralFilter(croppedImg,filteredImg,5,10,3);
        Imgproc.blur(croppedImg,filteredImg,new Size(15,15));
        Imgproc.medianBlur(filteredImg,filteredImg,11);
        Utils.matToBitmap(filteredImg,bmp);
        saveImageToExternalStorage(bmp);
        //STEP 3
        Core.subtract(whiteImg,filteredImg,negative);
        Utils.matToBitmap(negative,bmp);
        saveImageToExternalStorage(bmp);
//EXTRACTION PHASE
        //STEP 1
        //disk-shaped structuring element with 2 point radius (todo check whether it's right or not)
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(15,15), new Point(-1,-1));
        //Apply the specified morphology operation
        Imgproc.morphologyEx( negative, topHatted, Imgproc.MORPH_TOPHAT, element );// 5 = top hat
        Utils.matToBitmap(topHatted,bmp);
        saveImageToExternalStorage(bmp);
        //STEP 2
        Imgproc.equalizeHist(topHatted,topHattedEqualized);
        //image.convertTo( new_image , -1, alpha, beta );
        Utils.matToBitmap(topHattedEqualized,bmp);
        saveImageToExternalStorage(bmp);
        //STEP 3
        Imgproc.threshold(topHattedEqualized,binary, 150,255, Imgproc.THRESH_BINARY);
        Utils.matToBitmap(binary,bmp);
        saveImageToExternalStorage(bmp);
        //STEP 4
        //noise removal with component connected labels
        Mat stats =  new Mat();
        Mat centroids =  new Mat();
        Imgproc.connectedComponentsWithStats(binary,labeled,stats,centroids);

        //Convert Mat to matrix
        int [] statArray = new int[stats.rows()];
        for (int i = 0; i < stats.rows(); i++){
            statArray[i] = (int)stats.get(i,CC_STAT_AREA)[0];
        }

        for (int i = 0; i < labeled.rows(); i++){
            for (int j = 0; j < labeled.cols(); j++){
                if (statArray[(int)labeled.get(i,j)[0]] < 300)
                    binary.put(i,j,0);
            }
        }
        Utils.matToBitmap(binary,bmp);
        saveImageToExternalStorage(bmp);

        double[][] binaryMatrix = matToMatrix(binary);
        Pair<double[][],Object> p = enlarging(binaryMatrix,bmp.getHeight(),bmp.getWidth(),5);
        binary = convertMatrixToMat(p.m,bmp.getHeight(),bmp.getWidth());
        Utils.matToBitmap(binary,bmp);
        saveImageToExternalStorage(bmp);

        binaryMatrix = thinning(p.m,bmp.getHeight(),bmp.getWidth());
        binaryMatrix = contoursCleaning(binaryMatrix,bmp.getHeight(),bmp.getWidth());
        binary = convertMatrixToMat(binaryMatrix,bmp.getHeight(),bmp.getWidth());

        Imgproc.connectedComponentsWithStats(binary,labeled,stats,centroids);

        //Convert Mat to matrix
        statArray = new int[stats.rows()];
        for (int i = 0; i < stats.rows(); i++){
            statArray[i] = (int)stats.get(i,CC_STAT_AREA)[0];
        }

        for (int i = 0; i < labeled.rows(); i++){
            for (int j = 0; j < labeled.cols(); j++){
                if (statArray[(int)labeled.get(i,j)[0]] < 150)
                    binary.put(i,j,0);
            }
        }
        Utils.matToBitmap(binary,bmp);
        saveImageToExternalStorage(bmp);

        MatOfInt4 hough = new MatOfInt4();

        Imgproc.HoughLinesP(binary, hough, 2, Math.PI/180, 15, 20, 20);
        Imgproc.cvtColor(binary,finalImg,Imgproc.COLOR_GRAY2RGB);

        Segment []segments = new Segment[hough.rows()];
        for (int i = 0; i < hough.rows(); i++) {

            double[] val = hough.get(i, 0);
            Imgproc.line(finalImg, new Point(val[0], val[1]), new Point(val[2], val[3]), new Scalar(255, 0, 0), 6);
            segments[i] = new Segment(val[0], val[1], val[2], val[3]);
        }

        System.out.println("before single link");
        Cluster []lines = singleLinkClustering(segments, finalImg.width());
        System.out.println("afterSingleLink");

        Utils.matToBitmap(finalImg,bmp);
        saveImageToExternalStorage(bmp);

        //Rgb[][] mat = matToRgbMatrix(finalImg);
        //Pair<ArrayList<Point>,Mat> lifeStruct = leftLifeLineDetection(mat,finalImg,finalImg.height(),finalImg.width());
        //ArrayList<Point> lifeL= lifeStruct.m;


        //mat=matToRgbMatrix(lifeStruct.c);
        //Pair<ArrayList<Point>,Mat> heartStruct =leftHeartLineDetection(mat,finalImg,finalImg.height(),finalImg.width());
        //ArrayList<Point> heartL = heartStruct.m;


        //mat=matToRgbMatrix(heartStruct.c);
        //Pair<ArrayList<Point>,Mat> headStruct = leftHeadLineDetection(mat,finalImg,finalImg.height(),finalImg.width());
        //ArrayList<Point> headL= headStruct.m;

        //Utils.matToBitmap(headStruct.c,bmp);
        //saveImageToExternalStorage(bmp);

        Mat defImg = new Mat(bmp2.getHeight(),bmp2.getWidth(), CvType.CV_8UC3);

        Utils.bitmapToMat(bmp2,defImg);

        if (lines[0] != null) {
            for (int i = 0; i < lines[0].clusterElements-1; i++) {

                Imgproc.line(defImg, lines[0].segments[i].first, lines[0].segments[i].second, new Scalar(0, 255, 0), 10);
                Imgproc.line(defImg, lines[0].segments[i].second, lines[0].segments[i+1].first, new Scalar(0, 255, 0), 10);

            }
            Imgproc.line(defImg, lines[0].segments[lines[0].clusterElements-1].first, lines[0].segments[lines[0].clusterElements-1].second, new Scalar(0, 255, 0), 10);

        }

        if (lines[1] != null) {
            for (int i = 0; i < lines[1].clusterElements-1; i++) {

                Imgproc.line(defImg, lines[1].segments[i].first, lines[1].segments[i].second, new Scalar(0, 0, 255), 10);
                Imgproc.line(defImg, lines[1].segments[i].second, lines[1].segments[i+1].first, new Scalar(0, 0, 255), 10);

            }
            Imgproc.line(defImg, lines[1].segments[lines[1].clusterElements-1].first, lines[1].segments[lines[1].clusterElements-1].second, new Scalar(0, 0, 255), 10);

        }

        if (lines[2] != null) {
            for (int i = 0; i < lines[2].clusterElements-1; i++) {

                Imgproc.line(defImg, lines[2].segments[i].first, lines[2].segments[i].second, new Scalar(255, 0, 0), 10);
                Imgproc.line(defImg, lines[2].segments[i].second, lines[2].segments[i+1].first, new Scalar(255, 0, 0), 10);

            }
            Imgproc.line(defImg, lines[2].segments[lines[2].clusterElements-1].first, lines[2].segments[lines[2].clusterElements-1].second, new Scalar(255, 0, 0), 10);

        }
       // System.out.println("LUNGHEZZA LINEE : prima: "+getLineLength(heartL)+" seconda:"+ getLineLength(headL)+ " terza:" + getLineLength(lifeL));

        Utils.matToBitmap(defImg,bmp2);
        saveImageToExternalStorage(bmp2);

        Database db = new Database();
        String prediction =   db.heartArrL.get(1)+db.heartArrS.get(1)
                + db.headArrL.get(1)+db.headArrS.get(1)
                + db.lifeArrL.get(1)+db.lifeArrS.get(1);

        /*String prediction =   db.heartArrL.get((int)getLineLength(heartL))+db.heartArrS.get(slope(heartL))
                            + db.headArrL.get((int)getLineLength(headL))+db.headArrS.get(slope(headL))
                            + db.lifeArrL.get((int)getLineLength(lifeL))+db.lifeArrS.get(slope(lifeL));

*/
        return new Pair(bmp2, prediction);
    }

    private static int slope(ArrayList<Point> arr) {
        int slopecount=0;
        if(arr.get(arr.size()-1).x - arr.get(0).x ==0)
            slopecount = 90;
        else{
            double coeff =  ((arr.get(arr.size()-1).y - arr.get(0).y) / (arr.get(arr.size()-1).x - arr.get(0).x));
            slopecount =(int) Math.toDegrees(Math.atan(coeff));
        }


        return slopecount%180;
    }
    private static void saveImageToExternalStorage(Bitmap finalBitmap) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/palmy_2");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 100000;
        n = generator.nextInt(n);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "Image-" + timeStamp + n + ".png";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}

class Pair<T,S> {
    public T m;
    public S c;

    Pair(T m, S c) {
        this.m=m;
        this.c=c;
    }
}

class Segment{
    Point first, second;
    Segment(double x1, double y1, double x2, double y2){
        first = new Point(x1,y1);
        second = new Point(x2,y2);
    }
}

class Cluster {
    int tag, clusterElements = 0;
    Segment [] segments;

    public Cluster ( Segment s, int maximumSegmentsNumber, int tag) {
        this.segments = new Segment[maximumSegmentsNumber];
        this.segments[clusterElements] = s;
        clusterElements++;
        this.tag = tag;
    }

    public double initDistance(Cluster c){
        double euclideanDistanceFirstWithFirst = Math.sqrt(Math.pow(this.segments[0].first.x-c.segments[0].first.x,2)+Math.pow(this.segments[0].first.y-c.segments[0].first.y,2));
        double euclideanDistanceFirstWithSecond = Math.sqrt(Math.pow(this.segments[0].first.x-c.segments[0].second.x,2)+Math.pow(this.segments[0].first.y-c.segments[0].second.y,2));
        double euclideanDistanceSecondWithFirst = Math.sqrt(Math.pow(this.segments[0].second.x-c.segments[0].first.x,2)+Math.pow(this.segments[0].second.y-c.segments[0].first.y,2));
        double euclideanDistanceSecondWithSecond = Math.sqrt(Math.pow(this.segments[0].second.x-c.segments[0].second.x,2)+Math.pow(this.segments[0].second.y-c.segments[0].second.y,2));

        return Math.min( Math.min( Math.min( euclideanDistanceFirstWithFirst,
                                                                    euclideanDistanceFirstWithSecond),
                                                                    euclideanDistanceSecondWithFirst),
                                                                    euclideanDistanceSecondWithSecond);
    }

    public void addClusterSegments( Cluster c){
        int currentElementsNumber = this.clusterElements;
        int index = 0;

        for(int i = currentElementsNumber; i < c.clusterElements + currentElementsNumber;i++){
            this.segments[i]=c.segments[index];
            index++;
            this.clusterElements++;
        }
        c.clusterElements = 0;
    }
}
