package domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameMap {
    private final int width;
    private final int height;
    private final MapCell[][] cells;

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new MapCell[height][width]; // [y][x]
    }

    public void setCell(int x, int y, MapCell cell) {
        cells[y][x] = cell;
    }

    public MapCell getCell(int x, int y) {
        return cells[y][x];
    }
}
