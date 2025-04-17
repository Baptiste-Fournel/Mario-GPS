package application.components;

import domain.Coordinate;
import domain.MapElementType;

public class PathInterpreter {
    public static MapElementType determineTileType(Coordinate prev, Coordinate curr, Coordinate next) {
        boolean horizontal = (curr.y() == prev.y() && curr.y() == next.y());
        boolean vertical = (curr.x() == prev.x() && curr.x() == next.x());

        return (horizontal) ? MapElementType.ARRETE_HORIZONTAL :
                (vertical) ? MapElementType.ARRETE_VERTICAL :
                        MapElementType.NOEUD;
    }
}
