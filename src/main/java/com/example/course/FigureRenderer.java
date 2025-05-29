package com.example.course;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class FigureRenderer {
    private GraphicsContext gc;
    private ZBuffer zBuffer;
    private FigureManager figureManager;
    private TransformationHandler transformationHandler;
    private LightingHandler lightingHandler;
    private double rotationAngle;

    public FigureRenderer(GraphicsContext gc, ZBuffer zBuffer, FigureManager figureManager) {
        this.gc = gc;
        this.zBuffer = zBuffer;
        this.figureManager = figureManager;
        this.transformationHandler = new TransformationHandler();
        this.lightingHandler = new LightingHandler();
    }

    public void setupTransformationMatrix() {
        transformationHandler.setupTransformationMatrix();
    }

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public void draw() {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        zBuffer.clear();

        transformationHandler.applyTransformations(figureManager.getFigures(), rotationAngle);
        renderModel();
    }

    private void renderModel() {
        List<PolygonWithColor> allPolygons = new ArrayList<>();

        for (Figure figure : figureManager.getFigures()) {
            for (Vertex[] polygon : figure.transformedPolygons) {
                double avgDepth = 0;
                for (Vertex v : polygon) {
                    avgDepth += v.value.getZ();
                }
                avgDepth /= polygon.length;
                allPolygons.add(new PolygonWithColor(polygon, figure.color, avgDepth));
            }
        }

        allPolygons.sort((p1, p2) -> Double.compare(p2.avgDepth, p1.avgDepth));

        for (PolygonWithColor p : allPolygons) {
            if (!isBackFace(p.polygon)) {
                drawPolygonWithZBuffer(p.polygon, p.color);
            }
        }
    }

    private boolean isBackFace(Vertex[] polygon) {
        if (polygon.length < 3) return true;
        Vector v1 = polygon[1].value.subtract(polygon[0].value);
        Vector v2 = polygon[2].value.subtract(polygon[0].value);
        Vector normal = Vector.vectorMultiplication(v1, v2).normalize();
        Vector viewDir = new Vector(0, 0, -1);
        return normal.getX() * viewDir.getX() + normal.getY() * viewDir.getY() + normal.getZ() * viewDir.getZ() > 0;
    }

    private void drawPolygonWithZBuffer(Vertex[] polygon, Color baseColor) {
        if (polygon.length < 3) return;

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

        for (Vertex v : polygon) {
            int x = (int) v.value.getX();
            int y = (int) v.value.getY();
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        minX = Math.max(0, minX);
        maxX = Math.min((int) gc.getCanvas().getWidth() - 1, maxX);
        minY = Math.max(0, minY);
        maxY = Math.min((int) gc.getCanvas().getHeight() - 1, maxY);

        double[] intensities = lightingHandler.calculateIntensities(polygon);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (isPointInsidePolygon(x, y, polygon)) {
                    double z = interpolateZ(x, y, polygon);
                    if (zBuffer.checkAndUpdate(x, y, z)) {
                        double intensity = lightingHandler.interpolateIntensity(x, y, polygon, intensities);
                        Color shadedColor = baseColor.deriveColor(0, 1, intensity, 1);
                        gc.setFill(shadedColor);
                        gc.fillRect(x, y, 1, 1);
                    }
                }
            }
        }
    }

    private boolean isPointInsidePolygon(int x, int y, Vertex[] polygon) {
        int i, j;
        boolean inside = false;
        for (i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
            if (((polygon[i].value.getY() > y) != (polygon[j].value.getY() > y)) &&
                    (x < (polygon[j].value.getX() - polygon[i].value.getX()) * (y - polygon[i].value.getY()) /
                            (polygon[j].value.getY() - polygon[i].value.getY()) + polygon[i].value.getX())) {
                inside = !inside;
            }
        }
        return inside;
    }

    private double interpolateZ(int x, int y, Vertex[] polygon) {
        if (polygon.length != 3) {
            double avgZ = 0;
            for (Vertex v : polygon) {
                avgZ += v.value.getZ();
            }
            return avgZ / polygon.length;
        }

        double totalArea = calculateTriangleArea(polygon[0], polygon[1], polygon[2]);
        if (totalArea == 0) return polygon[0].value.getZ();

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

        return w1 * polygon[0].value.getZ() +
                w2 * polygon[1].value.getZ() +
                w3 * polygon[2].value.getZ();
    }

    private double calculateTriangleArea(Vertex v1, Vertex v2, Vertex v3) {
        double x1 = v2.value.getX() - v1.value.getX();
        double y1 = v2.value.getY() - v1.value.getY();
        double x2 = v3.value.getX() - v1.value.getX();
        double y2 = v3.value.getY() - v1.value.getY();
        return Math.abs(x1 * y2 - x2 * y1) / 2.0;
    }

    private static class PolygonWithColor {
        Vertex[] polygon;
        Color color;
        double avgDepth;

        PolygonWithColor(Vertex[] polygon, Color color, double avgDepth) {
            this.polygon = polygon;
            this.color = color;
            this.avgDepth = avgDepth;
        }
    }
}
