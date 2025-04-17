package components;

import application.ShortestPathUseCase.Coordinate;
import domain.GameMap;
import domain.MapElementType;

import java.util.List;

public class PathInterpreter {
    public static void applyPath(List<Coordinate> path, GameMap map) {
        if (path == null || path.size() < 2) return;

        for (int i = 1; i < path.size() - 1; i++) {
            Coordinate prev = path.get(i - 1);
            Coordinate curr = path.get(i);
            Coordinate next = path.get(i + 1);

            MapElementType typeToSet = determineTileType(prev, curr, next);
            map.getCell(curr.x(), curr.y()).setType(typeToSet);
        }
    }

    public static MapElementType determineTileType(Coordinate prev, Coordinate curr, Coordinate next) {
        boolean horizontal = (curr.y() == prev.y() && curr.y() == next.y());
        boolean vertical = (curr.x() == prev.x() && curr.x() == next.x());

        return (horizontal) ? MapElementType.ARRETE_HORIZONTAL :
                (vertical) ? MapElementType.ARRETE_VERTICAL :
                        MapElementType.NOEUD;
    }
}
