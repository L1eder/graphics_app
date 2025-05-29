package com.example.course;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {
    private static final int CANVAS_WIDTH = 1280;
    private static final int CANVAS_HEIGHT = 760;
    private double rotationAngle = 0;
    private double rotationSpeed = 0;
    private FigureRenderer renderer;
    private FigureManager figureManager;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("3D Figure Renderer");
        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Инициализация компонентов
        ZBuffer zBuffer = new ZBuffer(CANVAS_WIDTH, CANVAS_HEIGHT);
        figureManager = new FigureManager();
        renderer = new FigureRenderer(gc, zBuffer, figureManager);

        // Загрузка данных и настройка трансформаций
        figureManager.loadFigures();
        renderer.setupTransformationMatrix();

        AnimationTimer timer = new AnimationTimer() {
            private double lastTime = 0;

            @Override
            public void handle(long now) {
                double currentTime = now / 1_000_000_000.0;
                if (lastTime == 0) lastTime = currentTime;
                double deltaTime = currentTime - lastTime;
                lastTime = currentTime;

                rotationAngle += rotationSpeed * deltaTime;
                renderer.setRotationAngle(rotationAngle);
                renderer.draw();
            }
        };
        timer.start();

        // Управление поворотом
        canvas.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) {
                rotationSpeed = -60;
            } else if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) {
                rotationSpeed = 60;
            }
        });

        canvas.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT ||
                    event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) {
                rotationSpeed = 0;
            }
        });

        Group root = new Group();
        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root, CANVAS_WIDTH, CANVAS_HEIGHT, Color.AQUA));
        primaryStage.show();
        canvas.requestFocus();
        renderer.draw();
    }
}
