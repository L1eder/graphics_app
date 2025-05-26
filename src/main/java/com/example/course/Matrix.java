package com.example.course;

public class Matrix {
    private double[][] matrix = new double[4][4];

    public Matrix() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                matrix[i][j] = (i == j) ? 1 : 0;
            }
        }
    }

    public double get(int i, int j) {
        return matrix[i][j];
    }

    public void set(int i, int j, double value) {
        matrix[i][j] = value;
    }

    public Matrix multiply(Matrix other) {
        Matrix result = new Matrix();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    result.set(i, j, result.get(i, j) + this.get(i, k) * other.get(k, j));
                }
            }
        }
        return result;
    }

    public Vector multiply(Vector vector) {
        double x = vector.getX();
        double y = vector.getY();
        double z = vector.getZ();
        double w = 1;

        double newX = matrix[0][0] * x + matrix[0][1] * y + matrix[0][2] * z + matrix[0][3] * w;
        double newY = matrix[1][0] * x + matrix[1][1] * y + matrix[1][2] * z + matrix[1][3] * w;
        double newZ = matrix[2][0] * x + matrix[2][1] * y + matrix[2][2] * z + matrix[2][3] * w;

        return new Vector(newX, newY, newZ);
    }

    public static Matrix createYSpinMatrix(double rotationAngleInDegrees) {
        double rotationAngleInRadians = Math.toRadians(rotationAngleInDegrees);
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.set(0, 0, Math.cos(rotationAngleInRadians));
        rotationMatrix.set(0, 2, Math.sin(rotationAngleInRadians));
        rotationMatrix.set(2, 0, -Math.sin(rotationAngleInRadians));
        rotationMatrix.set(2, 2, Math.cos(rotationAngleInRadians));
        return rotationMatrix;
    }

    public static Matrix makeMatrixProjection(double l, double r, double b, double t, double n, double f) {
        Matrix projectionMatrix = new Matrix();
        projectionMatrix.set(0, 0, 2 / (r - l));
        projectionMatrix.set(1, 1, 2 / (t - b));
        projectionMatrix.set(2, 2, -2 / (f - n));
        projectionMatrix.set(0, 3, -(r + l) / (r - l));
        projectionMatrix.set(1, 3, -(t + b) / (t - b));
        projectionMatrix.set(2, 3, -(f + n) / (f - n));
        return projectionMatrix;
    }

    public static Matrix makeViewportMatrix(double x, double y, double w, double h, double depth) {
        Matrix viewportMatrix = new Matrix();
        viewportMatrix.set(0, 0, w / 2.0);
        viewportMatrix.set(1, 1, -h / 2.0);
        viewportMatrix.set(2, 2, depth / 2.0);
        viewportMatrix.set(0, 3, x + w / 2.0);
        viewportMatrix.set(1, 3, y + h / 2.0);
        return viewportMatrix;
    }

    public static Matrix makeLookAtMatrix(Vector rightV, Vector topV, Vector backV, Vector cameraPos) {
        Matrix lookAtMatrix = new Matrix();
        lookAtMatrix.set(0, 0, rightV.getX());
        lookAtMatrix.set(0, 1, rightV.getY());
        lookAtMatrix.set(0, 2, rightV.getZ());
        lookAtMatrix.set(1, 0, topV.getX());
        lookAtMatrix.set(1, 1, topV.getY());
        lookAtMatrix.set(1, 2, topV.getZ());
        lookAtMatrix.set(2, 0, backV.getX());
        lookAtMatrix.set(2, 1, backV.getY());
        lookAtMatrix.set(2, 2, backV.getZ());
        lookAtMatrix.set(0, 3, -cameraPos.getX());
        lookAtMatrix.set(1, 3, -cameraPos.getY());
        lookAtMatrix.set(2, 3, -cameraPos.getZ());
        return lookAtMatrix;
    }

    public static Matrix createScaleMatrix(double kx, double ky, double kz) {
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.set(0, 0, kx);
        scaleMatrix.set(1, 1, ky);
        scaleMatrix.set(2, 2, kz);
        return scaleMatrix;
    }
}
