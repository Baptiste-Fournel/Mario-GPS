package components;

import domain.GameMap;
import domain.MapElementType;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.Map;

public class MapRenderer {
    private final GameMap map;
    private final GraphicsContext graphics;
    private final Map<MapElementType, Image> tileImages;

    public MapRenderer(GameMap map, GraphicsContext graphics, Map<MapElementType, Image> tileImages) {
        this.map = map;
        this.graphics = graphics;
        this.tileImages = tileImages;
    }

    public void render() {
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                MapElementType type = map.getCell(x, y).getType();
                Image img = tileImages.get(type);
                if (img != null) {
                    graphics.drawImage(img, x * 64, y * 64);
                }
            }
        }
    }
}

