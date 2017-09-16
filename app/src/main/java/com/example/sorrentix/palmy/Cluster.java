package com.example.sorrentix.palmy;

import java.util.ArrayList;

/**
 * Created by orlandoaprea on 15/09/17.
 */

public class Cluster {
    int tag, clusterElements = 0;
    Segment [] segments;

    public Cluster ( Segment s, int maximumSegmentsNumber, int tag) {
        this.segments = new Segment[maximumSegmentsNumber];
        this.segments[clusterElements] = s;
        clusterElements++;
        this.tag = tag;
    }

    public double initDistance(Cluster c){
        double euclideanDistance = Math.sqrt(Math.pow(this.segments[0].puntoMedioX()-c.segments[0].puntoMedioX(),2)+Math.pow(this.segments[0].puntoMedioY()-c.segments[0].puntoMedioY(),2));
        double slopeDistance = Math.sqrt(Math.pow(this.segments[0].getSlope()-c.segments[0].getSlope(),2));
        return euclideanDistance + slopeDistance;
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
