package ui;

import application.PlaceStartPointUseCase;
import domain.GameMap;
import domain.MapCell;
import domain.MapElementType;
import infrastructure.GeoJsonExporter;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MapGeneratorUI extends Application {

    private static final int TILE_SIZE = 64;
    private static final int WIDTH = 10;
    private static final int HEIGHT = 10;

    private final Map<MapElementType, Image> tileImages = new HashMap<>();
    private final GameMap logicalMap = new GameMap(WIDTH, HEIGHT);
    private MapCell previousStartNode = null;
    private boolean selectingStartPoint = false;

    @Override
    public void start(Stage primaryStage) {
        loadTiles();

        Canvas canvas = new Canvas(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawRandomMap(gc);

        String defaultStyle = """
                    -fx-font-size: 16px;
                    -fx-font-family: 'Arial';
                    -fx-background-color: #f2d7a0;
                    -fx-border-color: #000000;
                    -fx-border-width: 2px;
                    -fx-background-radius: 8px;
                    -fx-padding: 12px;
                """;

        String activeStyle = """
                    -fx-font-size: 16px;
                    -fx-font-family: 'Arial';
                    -fx-background-color: #ffd700;
                    -fx-border-color: #ff0000;
                    -fx-border-width: 2px;
                    -fx-background-radius: 8px;
                    -fx-padding: 12px;
                """;

        Button startButton = new Button("Point de départ");
        startButton.setStyle(defaultStyle);

        PlaceStartPointUseCase useCase = new PlaceStartPointUseCase();

        startButton.setOnAction(e -> {
            selectingStartPoint = true;
            startButton.setStyle(activeStyle);
            canvas.setCursor(Cursor.CROSSHAIR);
        });

        canvas.setOnMouseClicked(event -> {
            if (!selectingStartPoint) return;

            int x = (int) (event.getX() / TILE_SIZE);
            int y = (int) (event.getY() / TILE_SIZE);
            if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) return;

            MapCell cell = logicalMap.getCell(x, y);
            if (cell.getType() == MapElementType.HERBE) {
                useCase.execute(logicalMap, x, y, previousStartNode);

                drawMap(gc);
                previousStartNode = logicalMap.getCell(x, y);
                selectingStartPoint = false;
                startButton.setStyle(defaultStyle);
                canvas.setCursor(Cursor.DEFAULT);

                exportToGeoJson();
            }
        });

        VBox controls = new VBox(20, startButton);
        controls.setAlignment(Pos.TOP_CENTER);
        controls.setPadding(new Insets(20));

        StackPane canvasContainer = new StackPane(canvas);
        canvasContainer.setPadding(new Insets(20));

        HBox root = new HBox(30, canvasContainer, controls);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Carte générée aléatoirement");
        primaryStage.setScene(scene);
        primaryStage.show();

        exportToGeoJson();
    }

    private void loadTiles() {
        for (MapElementType type : MapElementType.values()) {
            String fileName = "/tiles/" + type.name().toLowerCase() + ".png";
            try {
                Image image = new Image(Objects.requireNonNull(getClass().getResource(fileName)).toExternalForm());
                tileImages.put(type, image);
            } catch (Exception ignored) {
            }
        }
    }

    private void drawRandomMap(GraphicsContext gc) {
        Random random = new Random();
        List<MapElementType> types = new ArrayList<>();
        Collections.addAll(types,
                MapElementType.HERBE, MapElementType.HERBE, MapElementType.HERBE, MapElementType.HERBE,
                MapElementType.ARBRE, MapElementType.ARBRE,
                MapElementType.EAU);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                MapElementType type = types.get(random.nextInt(types.size()));
                logicalMap.setCell(x, y, new MapCell(x, y, type));
            }
        }

        drawMap(gc);
    }

    private void drawMap(GraphicsContext gc) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                MapElementType type = logicalMap.getCell(x, y).getType();
                Image img = tileImages.get(type);
                if (img != null) {
                    gc.drawImage(img, x * TILE_SIZE, y * TILE_SIZE);
                }
            }
        }
    }

    private void exportToGeoJson() {
        JSONObject geoJson = GeoJsonExporter.export(logicalMap);
        try {
            Files.write(Paths.get("generated-map.geojson"), geoJson.toString(2).getBytes());
        } catch (Exception ignored) {
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
