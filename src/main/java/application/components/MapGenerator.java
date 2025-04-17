package application.components;

import domain.GameMap;
import domain.MapCell;
import domain.MapElementType;

import java.util.List;
import java.util.Random;

public class MapGenerator {
    private static final List<MapElementType> weights = List.of(
            MapElementType.HERBE, MapElementType.HERBE, MapElementType.HERBE, MapElementType.HERBE, MapElementType.HERBE, MapElementType.HERBE,
            MapElementType.ARBRE,
            MapElementType.EAU
    );

    public static void generate(GameMap map) {
        Random random = new Random();
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                MapElementType type = weights.get(random.nextInt(weights.size()));
                map.setCell(x, y, new MapCell(x, y, type));
            }
        }
    }
}

