package presentation.ui;

import domain.Coordinate;
import application.usecases.PlaceEndPointUseCase;
import application.usecases.PlaceStartPointUseCase;
import domain.GameMap;
import domain.MapCell;
import domain.MapElementType;
import lombok.Getter;
import lombok.Setter;

public class MapInteractionHandler {

    private final PlaceStartPointUseCase startUseCase;
    private final PlaceEndPointUseCase endUseCase;

    @Getter
    private MapCell startNode;
    @Getter
    private MapCell endNode;

    @Getter
    @Setter
    private boolean selectingStart;
    @Getter
    @Setter
    private boolean selectingEnd;

    public MapInteractionHandler(PlaceStartPointUseCase startUseCase, PlaceEndPointUseCase endUseCase) {
        this.startUseCase = startUseCase;
        this.endUseCase = endUseCase;
    }

    public boolean handleClick(GameMap map, int x, int y, boolean isStartSelection, boolean isEndSelection) {
        if (x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight()) return false;

        MapCell cell = map.getCell(x, y);
        if (cell.getType() != MapElementType.HERBE && cell.getType() != MapElementType.START && cell.getType() != MapElementType.CHATEAU)
            return false;

        if (isStartSelection) {
            startUseCase.execute(map, x, y, startNode);
            startNode = map.getCell(x, y);
        } else if (isEndSelection && startNode != null) {
            endUseCase.execute(map, x, y, endNode);
            endNode = map.getCell(x, y);
        } else return false;

        return true;
    }

    public void reset() {
        startNode = null;
        endNode = null;
        selectingStart = false;
        selectingEnd = false;
    }

    public Coordinate getStartCoordinate() {
        return startNode == null ? null : new Coordinate(startNode.getX(), startNode.getY());
    }

    public Coordinate getEndCoordinate() {
        return endNode == null ? null : new Coordinate(endNode.getX(), endNode.getY());
    }
}
