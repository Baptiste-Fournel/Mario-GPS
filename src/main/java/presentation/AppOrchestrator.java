package presentation;

import application.components.MapGenerator;
import application.components.MapRenderer;
import application.usecases.PlaceEndPointUseCase;
import application.usecases.PlaceStartPointUseCase;
import domain.GameMap;
import domain.MapElementType;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import presentation.animation.MarioAnimator;
import presentation.ui.MapGeneratorUI;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AppOrchestrator extends Application {

    private static final int WIDTH = 30;
    private static final int HEIGHT = 30;

    @Override
    public void start(Stage primaryStage) {
        GameMap map = new GameMap(WIDTH, HEIGHT);
        MapGenerator.generate(map);

        Canvas canvas = new Canvas();
        GraphicsContext graphics = canvas.getGraphicsContext2D();

        Map<MapElementType, Image> tileImages = new HashMap<>();
        for (MapElementType type : MapElementType.values()) {
            try {
                String path = "/tiles/" + type.name().toLowerCase() + ".png";
                tileImages.put(type, new Image(Objects.requireNonNull(getClass().getResource(path)).toExternalForm()));
            } catch (Exception ignored) {
            }
        }

        Image marioImage = new Image(Objects.requireNonNull(getClass().getResource("/tiles/mario.png")).toExternalForm());
        Image castleImage = new Image(Objects.requireNonNull(getClass().getResource("/tiles/chateau.png")).toExternalForm());

        PlaceStartPointUseCase startUseCase = new PlaceStartPointUseCase();
        PlaceEndPointUseCase endUseCase = new PlaceEndPointUseCase();

        MapRenderer renderer = new MapRenderer(map, graphics, tileImages, 1);
        MapGeneratorUI ui = new MapGeneratorUI(
                map, canvas, tileImages, marioImage, castleImage,
                startUseCase, endUseCase, renderer, null
        );


        MarioAnimator animator = new MarioAnimator(graphics, renderer, map, marioImage, ui::drawSpecialImages);
        ui.setMarioAnimator(animator);

        Scene scene = new Scene(ui.getRootLayout());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

        primaryStage.setTitle("Mario GPS");
        primaryStage.setScene(scene);
        primaryStage.show();

        ui.onSceneReady(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}