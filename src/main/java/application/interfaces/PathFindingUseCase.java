package application.interfaces;

import domain.Coordinate;
import domain.GameMap;

import java.util.List;

public interface PathFindingUseCase {
    List<Coordinate> execute(GameMap map, Coordinate start, Coordinate goal);
}
