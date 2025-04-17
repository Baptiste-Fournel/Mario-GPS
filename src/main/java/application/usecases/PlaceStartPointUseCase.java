package application.usecases;

import domain.GameMap;
import domain.MapCell;
import domain.MapElementType;

public class PlaceStartPointUseCase {
    public void execute(GameMap map, int x, int y, MapCell previousStartNode) {
        if (previousStartNode != null) {
            previousStartNode.setType(MapElementType.HERBE);
        }
        map.getCell(x, y).setType(MapElementType.START);
    }
}
