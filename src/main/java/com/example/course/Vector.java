package com.example.course;

public class Vector {
    private double[] vector = {0, 0, 0, 1};

    public Vector(double x, double y, double z) {
        this.vector[0] = x;
        this.vector[1] = y;
        this.vector[2] = z;
    }

    public double getX() {
        return vector[0];
    }

    public double getY() {
        return vector[1];
    }

    public double getZ() {
        return vector[2];
    }

    public Vector subtract(Vector other) {
        return new Vector(this.getX() - other.getX(), this.getY() - other.getY(), this.getZ() - other.getZ());
    }

    public Vector normalize() {
        double length = Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
        return new Vector(vector[0] / length, vector[1] / length, vector[2] / length);
    }

    public static Vector vectorMultiplication(Vector firstVector, Vector secondVector) {
        return new Vector(
                firstVector.getY() * secondVector.getZ() - firstVector.getZ() * secondVector.getY(),
                firstVector.getZ() * secondVector.getX() - firstVector.getX() * secondVector.getZ(),
                firstVector.getX() * secondVector.getY() - firstVector.getY() * secondVector.getX()
        );
    }
}
