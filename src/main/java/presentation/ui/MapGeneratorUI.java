package presentation.ui;

import domain.Coordinate;
import application.components.MapRenderer;
import application.usecases.PlaceEndPointUseCase;
import application.usecases.PlaceStartPointUseCase;
import application.usecases.UpdateMapSizeUseCase;
import domain.GameMap;
import domain.MapCell;
import domain.MapElementType;
import infrastructure.GeoJsonExporter;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import presentation.animation.MarioAnimator;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MapGeneratorUI {

    private GameMap map;
    private final Canvas canvas;
    private final GraphicsContext graphics;
    private final Map<MapElementType, Image> tileImages;
    private final Image castleImage;
    private final Image marioImage;

    @Getter
    private MapRenderer renderer;
    private final MapInteractionHandler interactionHandler;
    private final PathCalculationController pathController;
    private final MapUIBuilder builder;

    @Getter
    private final HBox rootLayout;

    @Getter
    @Setter
    private MarioAnimator marioAnimator;

    private final UpdateMapSizeUseCase updateMapSizeUseCase;
    private int tileSize;

    public MapGeneratorUI(GameMap map, Canvas canvas, Map<MapElementType, Image> tileImages, Image marioImage, Image castleImage, PlaceStartPointUseCase startUseCase, PlaceEndPointUseCase endUseCase, MapRenderer renderer, MarioAnimator marioAnimator) {

        this.map = map;
        this.canvas = canvas;
        this.graphics = canvas.getGraphicsContext2D();
        this.tileImages = tileImages;
        this.marioImage = marioImage;
        this.castleImage = castleImage;
        this.renderer = renderer;
        this.marioAnimator = marioAnimator;
        this.updateMapSizeUseCase = new UpdateMapSizeUseCase();

        this.interactionHandler = new MapInteractionHandler(startUseCase, endUseCase);
        this.pathController = new PathCalculationController(map, renderer, interactionHandler);

        this.builder = new MapUIBuilder(canvas, interactionHandler, pathController, this);
        this.rootLayout = builder.build();

        attachCanvasClickHandler();
    }

    public void onSceneReady(Scene scene) {
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        scene.widthProperty().addListener((_, _, newVal) -> updateTileSizeAndRedraw(newVal.intValue(), (int) scene.getHeight()));
        scene.heightProperty().addListener((_, _, newVal) -> updateTileSizeAndRedraw((int) scene.getWidth(), newVal.intValue()));
        updateTileSizeAndRedraw((int) scene.getWidth(), (int) scene.getHeight());
        exportGeoJson();
    }

    private void updateTileSizeAndRedraw(int windowWidth, int windowHeight) {
        double canvasRatio = 0.8;
        int availableWidth = (int) (windowWidth * canvasRatio);
        int availableHeight = windowHeight - 40;
        tileSize = Math.min(availableWidth / map.getWidth(), availableHeight / map.getHeight());

        canvas.setWidth(tileSize * map.getWidth());
        canvas.setHeight(tileSize * map.getHeight());

        renderer.setTileSize(tileSize);
        renderer.render();
        drawSpecialImages(null);
    }

    public void drawSpecialImages(Coordinate marioPosition) {
        MapCell startNode = interactionHandler.getStartNode();
        MapCell endNode = interactionHandler.getEndNode();

        if (startNode != null)
            graphics.drawImage(tileImages.get(MapElementType.START), startNode.getX() * tileSize, startNode.getY() * tileSize, tileSize, tileSize);

        if (endNode != null)
            graphics.drawImage(castleImage, endNode.getX() * tileSize, endNode.getY() * tileSize, tileSize, tileSize);

        if (marioPosition != null)
            graphics.drawImage(marioImage, marioPosition.x() * tileSize, marioPosition.y() * tileSize, tileSize, tileSize);
    }

    public void clearCurrentPath() {
        List<Coordinate> path = pathController.getCurrentPath();
        Set<Coordinate> modified = pathController.getModifiedCells();

        for (Coordinate coord : modified) {
            MapCell cell = map.getCell(coord.x(), coord.y());
            cell.setType(MapElementType.HERBE);
        }

        path.clear();
        modified.clear();
    }

    public void regenerateMap(int newWidth, int newHeight) {
        this.map = updateMapSizeUseCase.execute(newWidth, newHeight);

        interactionHandler.reset();
        pathController.updateMap(map);

        this.renderer = new MapRenderer(map, graphics, tileImages, tileSize);
        pathController.updateRenderer(renderer);

        this.marioAnimator = new MarioAnimator(graphics, renderer, map, marioImage, this::drawSpecialImages);

        updateTileSizeAndRedraw((int) canvas.getScene().getWidth(), (int) canvas.getScene().getHeight());
        drawSpecialImages(null);
        exportGeoJson();

        attachCanvasClickHandler();
    }

    private void attachCanvasClickHandler() {
        canvas.setOnMouseClicked(event -> {
            int x = (int) event.getX() / tileSize;
            int y = (int) event.getY() / tileSize;

            if (interactionHandler.handleClick(map, x, y, builder.isSelectingStart(), builder.isSelectingEnd())) {
                clearCurrentPath();
                renderer.render();
                drawSpecialImages(null);
                exportGeoJson();
            }
        });
    }

    private void exportGeoJson() {
        try {
            JSONObject geoJson = GeoJsonExporter.export(map);
            Files.write(Paths.get("generated-map.geojson"), geoJson.toString(2).getBytes());
        } catch (Exception ignored) {
        }
    }
}