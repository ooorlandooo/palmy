package com.example.sorrentix.palmy;

/**
 * Created by ALESSANDROSERRAPICA on 15/09/2017.
 */

public class Rgb {
    private double red;
    private double green;
    private double blue;

    public Rgb (double r, double g, double b){

        this.red=r;
        this.green=g;
        this.blue = b;

    }

    public double getRed(){
        return red;
    }
    public double getGreen(){
        return green;
    }
    public double getBlue(){
        return blue;
    }

    public void setBlue(double blue) {
        this.blue = blue;
    }

    public void setGreen(double green) {
        this.green = green;
    }

    public void setRed(double red) {
        this.red = red;
    }
}
