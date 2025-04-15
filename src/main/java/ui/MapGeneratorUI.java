package ui;

import application.PlaceEndPointUseCase;
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
    private final GameMap map = new GameMap(WIDTH, HEIGHT);

    private MapCell startNode = null;
    private MapCell endNode = null;

    private boolean selectingStart = false;
    private boolean selectingEnd = false;

    private Canvas canvas;
    private GraphicsContext graphics;

    private Button startButton;
    private Button endButton;

    private final PlaceStartPointUseCase startUseCase = new PlaceStartPointUseCase();
    private final PlaceEndPointUseCase endUseCase = new PlaceEndPointUseCase();

    @Override
    public void start(Stage primaryStage) {
        initializeCanvas();
        loadTiles();
        generateRandomMap();

        VBox controlPanel = createControlPanel();
        HBox rootLayout = buildMainLayout(controlPanel);

        primaryStage.setTitle("Carte générée aléatoirement");
        primaryStage.setScene(new Scene(rootLayout));
        primaryStage.show();

        exportGeoJson();
    }

    private void initializeCanvas() {
        canvas = new Canvas(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        graphics = canvas.getGraphicsContext2D();
        canvas.setOnMouseClicked(event -> handleCanvasClick((int) (event.getX() / TILE_SIZE), (int) (event.getY() / TILE_SIZE)));
    }

    private void loadTiles() {
        for (MapElementType type : MapElementType.values()) {
            try {
                String path = "/tiles/" + type.name().toLowerCase() + ".png";
                tileImages.put(type, new Image(Objects.requireNonNull(getClass().getResource(path)).toExternalForm()));
            } catch (Exception ignored) {
            }
        }
    }

    private void generateRandomMap() {
        List<MapElementType> weights = List.of(
                MapElementType.HERBE, MapElementType.HERBE, MapElementType.HERBE, MapElementType.HERBE,
                MapElementType.ARBRE, MapElementType.ARBRE,
                MapElementType.EAU
        );

        Random random = new Random();
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                MapElementType type = weights.get(random.nextInt(weights.size()));
                map.setCell(x, y, new MapCell(x, y, type));
            }
        }

        renderMap();
    }

    private VBox createControlPanel() {
        startButton = createStyledButton("Point de départ");
        endButton = createStyledButton("Point d’arrivée");

        startButton.setOnAction(e -> activateStartSelection());
        endButton.setOnAction(e -> activateEndSelection());

        VBox box = new VBox(20, startButton, endButton);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(20));
        return box;
    }

    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(defaultButtonStyle());
        return btn;
    }

    private HBox buildMainLayout(VBox controls) {
        StackPane canvasContainer = new StackPane(canvas);
        canvasContainer.setPadding(new Insets(20));

        HBox layout = new HBox(30, canvasContainer, controls);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);
        return layout;
    }

    private void handleCanvasClick(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) return;

        MapCell cell = map.getCell(x, y);
        if (cell.getType() != MapElementType.HERBE) return;

        if (selectingStart) {
            startUseCase.execute(map, x, y, startNode);
            startNode = map.getCell(x, y);
            resetSelection();
            renderMap();
            exportGeoJson();
        } else if (selectingEnd && startNode != null) {
            endUseCase.execute(map, x, y, endNode);
            endNode = map.getCell(x, y);
            resetSelection();
            renderMap();
            exportGeoJson();
        }
    }

    private void activateStartSelection() {
        selectingStart = true;
        selectingEnd = false;
        startButton.setStyle(activeButtonStyle());
        endButton.setStyle(defaultButtonStyle());
        canvas.setCursor(Cursor.CROSSHAIR);
    }

    private void activateEndSelection() {
        if (startNode == null) return;
        selectingEnd = true;
        selectingStart = false;
        endButton.setStyle(activeButtonStyle());
        startButton.setStyle(defaultButtonStyle());
        canvas.setCursor(Cursor.CROSSHAIR);
    }

    private void resetSelection() {
        selectingStart = false;
        selectingEnd = false;
        startButton.setStyle(defaultButtonStyle());
        endButton.setStyle(defaultButtonStyle());
        canvas.setCursor(Cursor.DEFAULT);
    }

    private void renderMap() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                MapElementType type = map.getCell(x, y).getType();
                Image img = tileImages.get(type);
                if (img != null) {
                    graphics.drawImage(img, x * TILE_SIZE, y * TILE_SIZE);
                }
            }
        }
    }

    private void exportGeoJson() {
        JSONObject geoJson = GeoJsonExporter.export(map);
        try {
            Files.write(Paths.get("generated-map.geojson"), geoJson.toString(2).getBytes());
        } catch (Exception ignored) {
        }
    }

    private String defaultButtonStyle() {
        return """
                    -fx-font-size: 16px;
                    -fx-font-family: 'Arial';
                    -fx-background-color: #f2d7a0;
                    -fx-border-color: #000000;
                    -fx-border-width: 2px;
                    -fx-background-radius: 8px;
                    -fx-padding: 12px;
                """;
    }

    private String activeButtonStyle() {
        return """
                    -fx-font-size: 16px;
                    -fx-font-family: 'Arial';
                    -fx-background-color: #ffd700;
                    -fx-border-color: #ff0000;
                    -fx-border-width: 2px;
                    -fx-background-radius: 8px;
                    -fx-padding: 12px;
                """;
    }

    public static void main(String[] args) {
        launch(args);
    }
}