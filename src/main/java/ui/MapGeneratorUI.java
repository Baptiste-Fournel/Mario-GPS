package ui;

import domain.GameMap;
import domain.MapCell;
import domain.MapElementType;
import infrastructure.GeoJsonExporter;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
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
    private final GameMap logicalMap = new GameMap(WIDTH, HEIGHT); // Classe renommée

    @Override
    public void start(Stage primaryStage) {
        loadTiles();

        Canvas canvas = new Canvas(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        drawRandomMap(gc);
        exportToGeoJson();

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);
        primaryStage.setTitle("Carte générée aléatoirement");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadTiles() {
        for (MapElementType type : MapElementType.values()) {
            String fileName = "/tiles/" + type.name().toLowerCase() + ".png";
            try {
                Image image = new Image(getClass().getResource(fileName).toExternalForm());
                tileImages.put(type, image);
            } catch (Exception e) {
                System.out.println("Image non trouvée pour : " + type);
            }
        }
    }

    private void drawRandomMap(GraphicsContext gc) {
        Random random = new Random();
        List<MapElementType> drawableTypes = new ArrayList<>();
        Collections.addAll(drawableTypes,
                MapElementType.HERBE, MapElementType.HERBE, MapElementType.HERBE, MapElementType.HERBE,
                MapElementType.ARBRE, MapElementType.ARBRE,
                MapElementType.EAU);


        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                MapElementType type = drawableTypes.get(random.nextInt(drawableTypes.size()));
                Image img = tileImages.get(type);
                gc.drawImage(img, x * TILE_SIZE, y * TILE_SIZE);

                logicalMap.setCell(x, y, new MapCell(x, y, type));
            }
        }
    }

    private void exportToGeoJson() {
        JSONObject geoJson = GeoJsonExporter.export(logicalMap);
        try {
            Files.write(Paths.get("generated-map.geojson"), geoJson.toString(2).getBytes());
            System.out.println("✅ Export GeoJSON réussi dans: generated-map.geojson");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'export GeoJSON: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
