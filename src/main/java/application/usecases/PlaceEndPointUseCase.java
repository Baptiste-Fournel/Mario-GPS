package application.usecases;

import domain.GameMap;
import domain.MapCell;
import domain.MapElementType;

public class PlaceEndPointUseCase {
    public void execute(GameMap map, int x, int y, MapCell previousEndNode) {
        if (previousEndNode != null) {
            previousEndNode.setType(MapElementType.HERBE);
        }
        map.getCell(x, y).setType(MapElementType.CHATEAU);
    }
}
