package com.example.course;

import java.util.List;
import java.util.stream.Collectors;

public class TransformationHandler {
    private Matrix transformationMatrix;

    public void setupTransformationMatrix() {
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

    public void applyTransformations(List<Figure> figures, double rotationAngle) {
        Matrix rotation = Matrix.createYSpinMatrix(rotationAngle);
        Matrix finalMatrix = transformationMatrix.multiply(rotation);

        for (Figure figure : figures) {
            figure.transformedPolygons = applyMatrixToPolygons(figure.polygons, finalMatrix, rotation);
        }
    }

    private List<Vertex[]> applyMatrixToPolygons(List<Vertex[]> polygons, Matrix finalMatrix, Matrix rotation) {
        return polygons.stream()
                .map(poly -> {
                    Vertex[] transformed = new Vertex[poly.length];
                    for (int i = 0; i < poly.length; i++) {
                        Vector transformedValue = finalMatrix.multiply(poly[i].value);
                        Vector transformedNormal = rotation.multiply(poly[i].normal).normalize();
                        transformed[i] = new Vertex(transformedValue, transformedNormal);
                    }
                    return transformed;
                })
                .collect(Collectors.toList());
    }
}
