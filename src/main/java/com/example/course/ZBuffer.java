package com.example.course;

public class ZBuffer {
    private double[][] buffer;
    private int width;
    private int height;

    public ZBuffer(int width, int height) {
        this.width = width;
        this.height = height;
        buffer = new double[height][width];
        clear();
    }

    public void clear() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffer[y][x] = Double.MAX_VALUE;
            }
        }
    }

    public boolean checkAndUpdate(int x, int y, double z) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            if (z < buffer[y][x]) {
                buffer[y][x] = z;
                return true;
            }
        }
        return false;
    }
}
