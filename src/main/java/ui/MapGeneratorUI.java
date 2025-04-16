package ui;

import application.PathFindingUseCase;
import application.PlaceEndPointUseCase;
import application.PlaceStartPointUseCase;
import application.ShortestPathUseCase;
import application.ShortestPathUseCase.Coordinate;
import components.MapGenerator;
import components.MapRenderer;
import components.PathInterpreter;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
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
    private List<Coordinate> currentPath = new ArrayList<>();

    private boolean selectingStart = false;
    private boolean selectingEnd = false;

    private Canvas canvas;
    private GraphicsContext graphics;
    private MapRenderer renderer;

    private Button startButton;
    private Button endButton;

    private final PlaceStartPointUseCase startUseCase = new PlaceStartPointUseCase();
    private final PlaceEndPointUseCase endUseCase = new PlaceEndPointUseCase();
    private final PathFindingUseCase pathUseCase = new ShortestPathUseCase();

    @Override
    public void start(Stage primaryStage) {
        initializeCanvas();
        loadTiles();
        MapGenerator.generate(map);

        renderer = new MapRenderer(map, graphics, tileImages);
        renderer.render();

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
            } catch (Exception ignored) {}
        }
    }

    private VBox createControlPanel() {
        startButton = createStyledButton("Point de départ");
        endButton = createStyledButton("Point d’arrivée");
        Button calculateButton = createStyledButton("Calculer le chemin");
        Button resetButton = createStyledButton("Réinitialiser");

        startButton.setOnAction(e -> activateStartSelection());
        endButton.setOnAction(e -> activateEndSelection());
        calculateButton.setOnAction(e -> calculatePath());
        resetButton.setOnAction(e -> resetMap());

        VBox box = new VBox(20, startButton, endButton, calculateButton, resetButton);
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

        clearCurrentPath();

        if (selectingStart) {
            startUseCase.execute(map, x, y, startNode);
            startNode = map.getCell(x, y);
        } else if (selectingEnd && startNode != null) {
            endUseCase.execute(map, x, y, endNode);
            endNode = map.getCell(x, y);
        } else {
            return;
        }

        resetSelection();
        renderer.render();
        exportGeoJson();
    }

    private void calculatePath() {
        if (startNode == null || endNode == null) return;

        clearCurrentPath();

        List<Coordinate> path = pathUseCase.execute(map,
                new Coordinate(startNode.getX(), startNode.getY()),
                new Coordinate(endNode.getX(), endNode.getY()));

        if (path == null || path.isEmpty()) {
            showErrorDialog();
            return;
        }

        currentPath = path;
        PathInterpreter.applyPath(currentPath, map);

        renderer.render();
        exportGeoJson();
    }

    private void clearCurrentPath() {
        for (Coordinate coord : currentPath) {
            MapCell cell = map.getCell(coord.x(), coord.y());
            if (cell.getType() == MapElementType.ARRETE_HORIZONTAL ||
                    cell.getType() == MapElementType.ARRETE_VERTICAL ||
                    cell.getType() == MapElementType.NOEUD) {
                cell.setType(MapElementType.HERBE);
            }
        }
        currentPath.clear();
    }

    private void resetMap() {
        startNode = null;
        endNode = null;
        currentPath.clear();
        MapGenerator.generate(map);
        renderer.render();
        exportGeoJson();
    }

    private void exportGeoJson() {
        JSONObject geoJson = GeoJsonExporter.export(map);
        try {
            Files.write(Paths.get("generated-map.geojson"), geoJson.toString(2).getBytes());
        } catch (Exception ignored) {}
    }

    private void showErrorDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Chemin introuvable");
        alert.setHeaderText(null);
        alert.setContentText("Aucun chemin n'est possible entre le point de départ et d’arrivée.");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("""
            -fx-font-size: 14px;
            -fx-font-family: 'Segoe UI';
            -fx-background-color: #ffe0e0;
        """);
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #b00020;");

        alert.showAndWait();
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

    private void activateStartSelection() {
        selectingStart = true;
        selectingEnd = false;
        startButton.setStyle(activeButtonStyle());
        endButton.setStyle(defaultButtonStyle());
        canvas.setCursor(Cursor.CROSSHAIR);
    }

    private void activateEndSelection() {
        if (startNode == null) return;
        selectingStart = false;
        selectingEnd = true;
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

    public static void main(String[] args) {
        launch(args);
    }
}