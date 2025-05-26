package com.example.course;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Figure {
    public List<Vertex[]> polygons = new ArrayList<>();
    public List<Vertex[]> transformedPolygons = new ArrayList<>();
    public Color color;

    public Figure(Color color) {
        this.color = color;
    }
}
