package presentation.ui;

import application.PlaceEndPointUseCase;
import application.PlaceStartPointUseCase;
import application.ShortestPathUseCase;
import application.interfaces.PathFindingUseCase;
import application.ShortestPathUseCase.Coordinate;
import components.MapGenerator;
import components.MapRenderer;
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
import presentation.animation.MarioAnimator;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MapGeneratorUI extends Application {

    private static final int WIDTH = 30;
    private static final int HEIGHT = 30;

    private final Map<MapElementType, Image> tileImages = new HashMap<>();
    private Image marioImage;
    private Image castleImage;

    private int tileSize;
    private Canvas canvas;
    private GraphicsContext graphics;

    private GameMap map;
    private MapRenderer renderer;
    private MarioAnimator marioAnimator;

    private MapCell startNode;
    private MapCell endNode;
    private List<Coordinate> currentPath = new ArrayList<>();

    private boolean selectingStart;
    private boolean selectingEnd;

    private Button startButton;
    private Button endButton;

    private final PlaceStartPointUseCase startUseCase = new PlaceStartPointUseCase();
    private final PlaceEndPointUseCase endUseCase = new PlaceEndPointUseCase();
    private final PathFindingUseCase pathUseCase = new ShortestPathUseCase();

    @Override
    public void start(Stage primaryStage) {
        initializeCanvas();
        loadTiles();

        this.map = new GameMap(WIDTH, HEIGHT);
        MapGenerator.generate(map);

        VBox controlPanel = createControlPanel();
        HBox rootLayout = buildMainLayout(controlPanel);
        Scene scene = new Scene(rootLayout);

        primaryStage.setTitle("Mario GPS");
        primaryStage.setScene(scene);
        primaryStage.show();

        updateTileSizeAndRedraw((int) scene.getWidth(), (int) scene.getHeight());

        scene.widthProperty().addListener((_, _, newVal) ->
                updateTileSizeAndRedraw(newVal.intValue(), (int) scene.getHeight()));
        scene.heightProperty().addListener((_, _, newVal) ->
                updateTileSizeAndRedraw((int) scene.getWidth(), newVal.intValue()));

        exportGeoJson();
    }

    private void initializeCanvas() {
        canvas = new Canvas();
        graphics = canvas.getGraphicsContext2D();
        canvas.setOnMouseClicked(event -> handleCanvasClick((int) event.getX() / tileSize, (int) event.getY() / tileSize));
    }

    private void loadTiles() {
        for (MapElementType type : MapElementType.values()) {
            try {
                String path = "/tiles/" + type.name().toLowerCase() + ".png";
                tileImages.put(type, new Image(Objects.requireNonNull(getClass().getResource(path)).toExternalForm()));
            } catch (Exception ignored) {}
        }

        marioImage = new Image(Objects.requireNonNull(getClass().getResource("/tiles/mario.png")).toExternalForm());
        castleImage = new Image(Objects.requireNonNull(getClass().getResource("/tiles/chateau.png")).toExternalForm());
    }

    private VBox createControlPanel() {
        startButton = createStyledButton("Point de départ");
        endButton = createStyledButton("Point d’arrivée");
        Button calculateButton = createStyledButton("Calculer le chemin");
        Button resetButton = createStyledButton("Réinitialiser");

        startButton.setOnAction(e -> setSelectionMode(true, false));
        endButton.setOnAction(e -> setSelectionMode(false, true));
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

    private void updateTileSizeAndRedraw(int windowWidth, int windowHeight) {
        double canvasRatio = 0.8;
        int availableWidth = (int) (windowWidth * canvasRatio);
        int availableHeight = windowHeight - 40;
        tileSize = Math.min(availableWidth / WIDTH, availableHeight / HEIGHT);

        canvas.setWidth(tileSize * WIDTH);
        canvas.setHeight(tileSize * HEIGHT);

        renderer = new MapRenderer(map, graphics, tileImages, tileSize);
        marioAnimator = new MarioAnimator(
                graphics,
                renderer,
                map,
                marioImage,
                this::drawSpecialImages
        );
        renderer.render();
        drawSpecialImages(null);
    }

    private void handleCanvasClick(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) return;

        MapCell cell = map.getCell(x, y);
        if (cell.getType() != MapElementType.HERBE &&
                cell.getType() != MapElementType.START &&
                cell.getType() != MapElementType.CHATEAU) return;

        clearCurrentPath();

        if (selectingStart) {
            startUseCase.execute(map, x, y, startNode);
            startNode = map.getCell(x, y);
        } else if (selectingEnd && startNode != null) {
            endUseCase.execute(map, x, y, endNode);
            endNode = map.getCell(x, y);
        } else return;

        resetSelection();
        renderer.render();
        drawSpecialImages(null);
        exportGeoJson();
    }

    private void calculatePath() {
        if (startNode == null || endNode == null) return;

        clearCurrentPath();

        Coordinate start = new Coordinate(startNode.getX(), startNode.getY());
        Coordinate end = new Coordinate(endNode.getX(), endNode.getY());

        currentPath = pathUseCase.execute(map, start, end);
        if (currentPath == null || currentPath.isEmpty()) {
            showErrorDialog();
            return;
        }

        marioAnimator.animate(currentPath, () -> {
            renderer.render();
            drawSpecialImages(null);
        });
    }

    private void drawSpecialImages(Coordinate marioPosition) {
        if (startNode != null)
            graphics.drawImage(tileImages.get(MapElementType.START), startNode.getX() * tileSize, startNode.getY() * tileSize, tileSize, tileSize);

        if (endNode != null)
            graphics.drawImage(castleImage, endNode.getX() * tileSize, endNode.getY() * tileSize, tileSize, tileSize);

        if (marioPosition != null)
            graphics.drawImage(marioImage, marioPosition.x() * tileSize, marioPosition.y() * tileSize, tileSize, tileSize);
    }

    private void clearCurrentPath() {
        for (Coordinate coord : currentPath) {
            MapCell cell = map.getCell(coord.x(), coord.y());
            if (EnumSet.of(MapElementType.ARRETE_HORIZONTAL, MapElementType.ARRETE_VERTICAL, MapElementType.NOEUD).contains(cell.getType())) {
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
        drawSpecialImages(null);
        exportGeoJson();
    }

    private void exportGeoJson() {
        try {
            JSONObject geoJson = GeoJsonExporter.export(map);
            Files.write(Paths.get("generated-map.geojson"), geoJson.toString(2).getBytes());
        } catch (Exception ignored) {}
    }

    private void showErrorDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Chemin introuvable");
        alert.setHeaderText(null);
        alert.setContentText("Aucun chemin n'est possible entre le point de départ et d’arrivée.");
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-font-size: 14px; -fx-font-family: 'Segoe UI'; -fx-background-color: #ffe0e0;");
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

    private void setSelectionMode(boolean start, boolean end) {
        selectingStart = start;
        selectingEnd = end;
        startButton.setStyle(start ? activeButtonStyle() : defaultButtonStyle());
        endButton.setStyle(end ? activeButtonStyle() : defaultButtonStyle());
        canvas.setCursor(start || end ? Cursor.CROSSHAIR : Cursor.DEFAULT);
    }

    private void resetSelection() {
        setSelectionMode(false, false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}