package com.example.carola.smartwatchnavigation;

/**
 * Created by Carola on 18.09.16.
 */
public class PathInforamtion {
    public double m1,m2;
    public double tan;
    public double angle;
    public double lenght;

    public PathInforamtion(double m1,double m2, double tan, double angle) {
        this.m1 = m1;
        this.m2 = m2;
        this.tan = tan;
        this.angle = angle;
    }

    public PathInforamtion(double angle, double lenght) {
        this.angle = angle;
        this.lenght = lenght;
    }
}
