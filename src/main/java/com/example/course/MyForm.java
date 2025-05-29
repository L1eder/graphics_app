package com.example.course;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MyForm extends Application {
    private static final String FILE_NAME = "src/main/java/com/example/course/7.obj";
    private Figure pyramid = new Figure(Color.rgb(0, 0, 100));
    private Figure cone = new Figure(Color.rgb(0, 255, 0));
    private Figure star = new Figure(Color.rgb(255, 255, 0));
    private double rotationAngle = 0;
    private Matrix transformationMatrix;
    private ZBuffer zBuffer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("3D Figure Renderer");
        Canvas canvas = new Canvas(1280, 760);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Инициализируем Z-буфер
        zBuffer = new ZBuffer((int)canvas.getWidth(), (int)canvas.getHeight());

        loadFile();
        setupTransformationMatrix();

        canvas.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) {
                rotationAngle -= 5;
                draw(gc);
            } else if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) {
                rotationAngle += 5;
                draw(gc);
            }
        });

        Group root = new Group();
        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root, 1280, 760, Color.AQUA));
        primaryStage.show();
        canvas.requestFocus();
        draw(gc);
    }

    private void draw(GraphicsContext gc) {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        zBuffer.clear(); // Очищаем Z-буфер перед новым рендерингом

        applyTransformations();
        renderModel(gc);
    }

    private void loadFile() {
        List<Vector> vertices = new ArrayList<>();
        List<Vector> normals = new ArrayList<>();
        String currentFigure = "";

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split(" ");
                if (parts[0].equals("o")) {
                    currentFigure = parts[1];
                    continue;
                }
                if (parts[0].equals("v")) {
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);
                    vertices.add(new Vector(x, y, z));
                } else if (parts[0].equals("vn")) {
                    normals.add(new Vector(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3])));
                } else if (parts[0].equals("f")) {
                    List<Vertex> polygon = new ArrayList<>();
                    for (int i = 1; i < parts.length; i++) {
                        String[] indices = parts[i].split("/");
                        int vertexIndex = Integer.parseInt(indices[0]) - 1;
                        int normalIndex = Integer.parseInt(indices[2]) - 1;
                        polygon.add(new Vertex(vertices.get(vertexIndex), normals.get(normalIndex)));
                    }
                    if (currentFigure.equals("Solid")) {
                        pyramid.polygons.add(polygon.toArray(new Vertex[0]));
                    } else if (currentFigure.equals("Окружность")) {
                        star.polygons.add(polygon.toArray(new Vertex[0]));
                    } else if (currentFigure.equals("Конус")) {
                        cone.polygons.add(polygon.toArray(new Vertex[0]));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupTransformationMatrix() {
        double mnX = -1, mxX = 1, mnY = -1, mxY = 1, mnZ = -1, mxZ = 1;
        Matrix viewport = Matrix.makeViewportMatrix(360, 210, 1280, 760, mxZ - mnZ);
        Matrix projection = Matrix.makeMatrixProjection(mnX, mxX, mnY, mxY, mxZ, mnZ);
        Vector cameraPos = new Vector(0.6, -0.4, 0.8);
        Vector center = new Vector(0, 0, 0);
        Vector zAxis = cameraPos.subtract(center).normalize();
        Vector xAxis = Vector.vectorMultiplication(new Vector(0, 1, 0), zAxis).normalize();
        Vector yAxis = Vector.vectorMultiplication(zAxis, xAxis).normalize();
        Matrix lookAt = Matrix.makeLookAtMatrix(xAxis, yAxis, zAxis, cameraPos);
        Matrix scale = Matrix.createScaleMatrix(0.2, 0.5, 0.2);
        transformationMatrix = viewport.multiply(projection).multiply(lookAt).multiply(scale);
    }

    private void applyTransformations() {
        Matrix rotation = Matrix.createYSpinMatrix(rotationAngle);

        pyramid.transformedPolygons = applyRotationToPolygons(pyramid.polygons, rotation);
        cone.transformedPolygons = applyRotationToPolygons(cone.polygons, rotation);
        star.transformedPolygons = applyRotationToPolygons(star.polygons, rotation);
    }

    private List<Vertex[]> applyRotationToPolygons(List<Vertex[]> polygons, Matrix rotation) {
        return polygons.stream()
                .map(poly -> {
                    Vertex[] transformed = new Vertex[poly.length];
                    for (int i = 0; i < poly.length; i++) {
                        Vector transformedValue = transformationMatrix.multiply(rotation).multiply(poly[i].value);
                        Vector transformedNormal = rotation.multiply(poly[i].normal).normalize();
                        transformed[i] = new Vertex(transformedValue, transformedNormal);
                    }
                    return transformed;
                })
                .collect(Collectors.toList());
    }

    private void renderModel(GraphicsContext gc) {
        List<PolygonWithColor> allPolygons = new ArrayList<>();

        for (Figure figure : List.of(pyramid, cone, star)) {
            for (Vertex[] polygon : figure.transformedPolygons) {
                double avgDepth = 0;
                for (Vertex v : polygon) {
                    avgDepth += v.value.getZ();
                }
                avgDepth /= polygon.length;
                allPolygons.add(new PolygonWithColor(polygon, figure.color, avgDepth));
            }
        }

        // Сортировка по глубине для оптимизации
        allPolygons.sort((p1, p2) -> Double.compare(p2.avgDepth, p1.avgDepth));

        for (PolygonWithColor p : allPolygons) {
            drawPolygonWithZBuffer(p.polygon, gc, p.color);
        }
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

    // Новый метод для отрисовки полигона с Z-буфером и освещением по Гуро
    private void drawPolygonWithZBuffer(Vertex[] polygon, GraphicsContext gc, Color baseColor) {
        if (polygon.length < 3) return;

        // Находим минимальные и максимальные координаты для ограничивающего прямоугольника
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

        for (Vertex v : polygon) {
            int x = (int)v.value.getX();
            int y = (int)v.value.getY();
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        // Ограничиваем область сканирования
        minX = Math.max(0, minX);
        maxX = Math.min((int)gc.getCanvas().getWidth() - 1, maxX);
        minY = Math.max(0, minY);
        maxY = Math.min((int)gc.getCanvas().getHeight() - 1, maxY);

        // Источник света
        Vector lightDir = new Vector(0, 0, -1).normalize();

        // Для освещения по Гуро вычисляем интенсивность для каждой вершины
        double[] intensities = new double[polygon.length];
        for (int i = 0; i < polygon.length; i++) {
            Vector normal = polygon[i].normal.normalize();
            intensities[i] = Math.max(0, normal.getX() * lightDir.getX() +
                    normal.getY() * lightDir.getY() +
                    normal.getZ() * lightDir.getZ());
        }

        // Сканируем область полигона
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (isPointInsidePolygon(x, y, polygon)) {
                    // Интерполируем Z-координату
                    double z = interpolateZ(x, y, polygon);
                    if (zBuffer.checkAndUpdate(x, y, z)) {
                        // Интерполируем интенсивность для освещения по Гуро
                        double intensity = interpolateIntensity(x, y, polygon, intensities);
                        Color shadedColor = baseColor.deriveColor(0, 1, intensity, 1);
                        gc.setFill(shadedColor);
                        gc.fillRect(x, y, 1, 1);
                    }
                }
            }
        }
    }

    // Проверка, находится ли точка внутри полигона
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

    // Интерполяция Z-координаты
    private double interpolateZ(int x, int y, Vertex[] polygon) {
        // Используем барицентрические координаты для интерполяции (работает только для треугольников)
        if (polygon.length != 3) {
            // Для полигонов с количеством вершин != 3 возвращаем среднее значение
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

    // Интерполяция интенсивности освещения
    private double interpolateIntensity(int x, int y, Vertex[] polygon, double[] intensities) {
        if (polygon.length != 3) {
            // Для полигонов с количеством вершин != 3 возвращаем среднее значение
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

    // Вычисление площади треугольника
    private double calculateTriangleArea(Vertex v1, Vertex v2, Vertex v3) {
        double x1 = v2.value.getX() - v1.value.getX();
        double y1 = v2.value.getY() - v1.value.getY();
        double x2 = v3.value.getX() - v1.value.getX();
        double y2 = v3.value.getY() - v1.value.getY();
        return Math.abs(x1 * y2 - x2 * y1) / 2.0;
    }
}
