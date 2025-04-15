package application;

import domain.GameMap;
import domain.MapCell;
import domain.MapElementType;

public class PlaceStartPointUseCase {

    public void execute(GameMap map, int x, int y, MapCell previousStartNode) {
        if (previousStartNode != null) {
            previousStartNode.setType(MapElementType.HERBE);
        }

        MapCell target = map.getCell(x, y);
        if (target.getType() == MapElementType.HERBE) {
            target.setType(MapElementType.NOEUD);
        }
    }
}
