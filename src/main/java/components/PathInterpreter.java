package components;

import application.ShortestPathUseCase.Coordinate;
import domain.GameMap;
import domain.MapElementType;

import java.util.List;

public class PathInterpreter {
    public static void applyPath(List<Coordinate> path, GameMap map) {
        if (path == null || path.isEmpty()) return;

        Coordinate prev = path.get(0);

        for (int i = 0; i < path.size(); i++) {
            Coordinate curr = path.get(i);
            Coordinate next = (i + 1 < path.size()) ? path.get(i + 1) : null;

            MapElementType typeToSet = determineTileType(prev, curr, next);
            map.getCell(curr.x(), curr.y()).setType(typeToSet);

            prev = curr;
        }
    }

    private static MapElementType determineTileType(Coordinate prev, Coordinate curr, Coordinate next) {
        if (next == null) return MapElementType.NOEUD;

        boolean horizontal = (curr.y() == prev.y() && curr.y() == next.y());
        boolean vertical = (curr.x() == prev.x() && curr.x() == next.x());

        return (horizontal) ? MapElementType.ARRETE_HORIZONTAL :
                (vertical) ? MapElementType.ARRETE_VERTICAL :
                        MapElementType.NOEUD;
    }
}

