package application.usecases;

import application.components.MapGenerator;
import domain.GameMap;

public class UpdateMapSizeUseCase {
    public GameMap execute(int width, int height) {
        GameMap newMap = new GameMap(width, height);
        MapGenerator.generate(newMap);
        return newMap;
    }
}
