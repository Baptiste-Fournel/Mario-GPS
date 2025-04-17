package domain;

import lombok.Getter;

@Getter
public class GameMap {
    private final MapCell[][] cells;
    private final int width;
    private final int height;

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new MapCell[height][width];
    }

    public void setCell(int x, int y, MapCell cell) {
        cells[y][x] = cell;
    }

    public MapCell getCell(int x, int y) {
        return cells[y][x];
    }
}
