package com.example.course;

public class LightingHandler {
    private Vector lightDir;

    public LightingHandler() {
        this.lightDir = new Vector(0, 0, -1).normalize();
    }

    public double[] calculateIntensities(Vertex[] polygon) {
        double[] intensities = new double[polygon.length];
        for (int i = 0; i < polygon.length; i++) {
            Vector normal = polygon[i].normal.normalize();
            intensities[i] = Math.max(0, normal.getX() * lightDir.getX() +
                    normal.getY() * lightDir.getY() +
                    normal.getZ() * lightDir.getZ());
        }
        return intensities;
    }

    public double interpolateIntensity(int x, int y, Vertex[] polygon, double[] intensities) {
        if (polygon.length != 3) {
            double avgIntensity = 0;
            for (double intensity : intensities) {
                avgIntensity += intensity;
            }
            return avgIntensity / intensities.length;
        }

        double totalArea = calculateTriangleArea(polygon[0], polygon[1], polygon[2]);
        if (totalArea == 0) return intensities[0];

        double area1 = calculateTriangleArea(new Vertex(new Vector(x, y, 0), new Vector(0, 0, 0)),
                polygon[1], polygon[2]);
        double area2 = calculateTriangleArea(polygon[0],
                new Vertex(new Vector(x, y, 0), new Vector(0, 0, 0)),
                polygon[2]);
        double area3 = calculateTriangleArea(polygon[0], polygon[1],
                new Vertex(new Vector(x, y, 0), new Vector(0, 0, 0)));

        double w1 = area1 / totalArea;
        double w2 = area2 / totalArea;
        double w3 = area3 / totalArea;

        return w1 * intensities[0] + w2 * intensities[1] + w3 * intensities[2];
    }

    private double calculateTriangleArea(Vertex v1, Vertex v2, Vertex v3) {
        double x1 = v2.value.getX() - v1.value.getX();
        double y1 = v2.value.getY() - v1.value.getY();
        double x2 = v3.value.getX() - v1.value.getX();
        double y2 = v3.value.getY() - v1.value.getY();
        return Math.abs(x1 * y2 - x2 * y1) / 2.0;
    }
}
