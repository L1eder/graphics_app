package com.example.course;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Figure {
    public List<Vertex[]> polygons = new ArrayList<>();
    public List<Vertex[]> transformedPolygons = new ArrayList<>();
    public Color color;
    public double averageDepth;

    public Figure(Color color) {
        this.color = color;
    }

    public double getAverageDepth() {
        double sumZ = 0;
        int count = 0;
        for (Vertex[] polygon : transformedPolygons) {
            for (Vertex vertex : polygon) {
                sumZ += vertex.value.getZ();
                count++;
            }
        }
        return count > 0 ? sumZ / count : 0;
    }

}
