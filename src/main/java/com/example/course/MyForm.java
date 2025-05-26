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

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("3D Figure Renderer");
        Canvas canvas = new Canvas(1280, 760);
        GraphicsContext gc = canvas.getGraphicsContext2D();

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
        Matrix viewport = Matrix.makeViewportMatrix(250, -200, 1280, 760, mxZ - mnZ);
        Matrix projection = Matrix.makeMatrixProjection(mnX, mxX, mnY, mxY, mxZ, mnZ);
        Vector cameraPos = new Vector(0.6, 0.4, 0.8);
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
                        Vector transformedNormal = rotation.multiply(poly[i].normal).normalize();  // трансформируем нормаль только вращением
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

        allPolygons.sort((p1, p2) -> Double.compare(p2.avgDepth, p1.avgDepth));

        for (PolygonWithColor p : allPolygons) {
            phongShading(p.polygon, gc, p.color);
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


    private void phongShading(Vertex[] polygon, GraphicsContext gc, Color baseColor) {
        Vector lightDir = new Vector(0, 0, -1).normalize();

        Vector avgNormal = new Vector(0, 0, 0);
        for (Vertex v : polygon) {
            avgNormal = new Vector(
                    avgNormal.getX() + v.normal.getX(),
                    avgNormal.getY() + v.normal.getY(),
                    avgNormal.getZ() + v.normal.getZ()
            );
        }
        avgNormal = avgNormal.normalize();

        double intensity = Math.max(0, avgNormal.getX() * lightDir.getX() +
                avgNormal.getY() * lightDir.getY() +
                avgNormal.getZ() * lightDir.getZ());

        Color shadedColor = baseColor.deriveColor(0, 1, intensity, 1);

        double[] xPoints = new double[polygon.length];
        double[] yPoints = new double[polygon.length];
        for (int i = 0; i < polygon.length; i++) {
            xPoints[i] = polygon[i].value.getX();
            yPoints[i] = polygon[i].value.getY();
        }

        gc.setFill(shadedColor);
        gc.fillPolygon(xPoints, yPoints, polygon.length);
    }

}
