package com.example.sorrentix.palmy;

/**
 * Created by sorrentix on 15/09/2017.
 */

public class Line {
    private Segment segment;
    private double slope;
    private double locX;
    private double locY;

    public Line(Segment segment, double slope, double locX, double locY) {
        this.segment = segment;
        this.slope = slope;
        this.locX = locX;
        this.locY = locY;
    }


    public Segment getSegment() {
        return segment;
    }

    public void setSegment(Segment segment) {
        this.segment = segment;
    }

    public double getSlope() {
        return slope;
    }

    public void setSlope(double slope) {
        this.slope = slope;
    }

    public double getLocX() {
        return locX;
    }

    public void setLocX(double locX) {
        this.locX = locX;
    }

    public double getLocY() {
        return locY;
    }

    public void setLocY(double locY) {
        this.locY = locY;
    }
}
