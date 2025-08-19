package com.example.course;

import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FigureManager {
    private static final String FILE_NAME = "src/main/java/com/example/course/4.obj";
    private Figure pyramid = new Figure(Color.rgb(0, 0, 200));
    private Figure cone = new Figure(Color.rgb(0, 255, 0));
    private Figure star = new Figure(Color.rgb(255, 255, 0));

    public void loadFigures() {
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
                    } else if (currentFigure.equals("Circle")) {
                        star.polygons.add(polygon.toArray(new Vertex[0]));
                    } else if (currentFigure.equals("Cube")) {
                        cone.polygons.add(polygon.toArray(new Vertex[0]));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Figure> getFigures() {
        List<Figure> figures = new ArrayList<>();
        figures.add(pyramid);
        figures.add(cone);
        figures.add(star);
        return figures;
    }
}
