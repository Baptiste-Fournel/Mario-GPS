package application.interfaces;

import domain.GameMap;
import application.ShortestPathUseCase.Coordinate;

import java.util.List;

public interface PathFindingUseCase {
    List<Coordinate> execute(GameMap map, Coordinate start, Coordinate goal);
}
