package application;

import domain.GameMap;
import domain.MapCell;
import domain.MapElementType;

public class PlaceEndPointUseCase {

    public void execute(GameMap map, int x, int y, MapCell previousEndNode) {
        if (previousEndNode != null) {
            previousEndNode.setType(MapElementType.HERBE);
        }

        MapCell target = map.getCell(x, y);
        if (target.getType() == MapElementType.HERBE) {
            target.setType(MapElementType.NOEUD);
        }
    }
}
