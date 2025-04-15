package application;

import domain.GameMap;
import domain.MapCell;
import domain.MapElementType;

import java.util.*;

public class ShortestPathUseCase {

    public record Coord(int x, int y) {}

    private static final List<Coord> DIRECTIONS = List.of(
            new Coord(0, -1),
            new Coord(0, 1),
            new Coord(-1, 0),
            new Coord(1, 0)
    );

    public List<Coord> execute(GameMap map, Coord start, Coord end) {
        int width = map.getWidth();
        int height = map.getHeight();

        Map<Coord, Coord> previous = new HashMap<>();
        Map<Coord, Integer> distances = new HashMap<>();
        PriorityQueue<Coord> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        distances.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            Coord current = queue.poll();

            if (current.equals(end)) {
                return reconstructPath(previous, end);
            }

            for (Coord dir : DIRECTIONS) {
                Coord neighbor = new Coord(current.x + dir.x, current.y + dir.y);
                if (!isInBounds(neighbor, width, height)) continue;

                MapCell cell = map.getCell(neighbor.x, neighbor.y);
                if (!isTraversable(cell)) continue;

                int newDist = distances.get(current) + 1;
                if (newDist < distances.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    distances.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        return null;
    }

    private boolean isInBounds(Coord c, int w, int h) {
        return c.x >= 0 && c.x < w && c.y >= 0 && c.y < h;
    }

    private boolean isTraversable(MapCell cell) {
        return cell.getType() == MapElementType.HERBE ||
                cell.getType() == MapElementType.NOEUD ||
                cell.getType() == MapElementType.ARRETE_HORIZONTAL ||
                cell.getType() == MapElementType.ARRETE_VERTICAL;
    }

    private List<Coord> reconstructPath(Map<Coord, Coord> previous, Coord target) {
        List<Coord> path = new ArrayList<>();
        Coord current = target;
        while (previous.containsKey(current)) {
            path.add(current);
            current = previous.get(current);
        }
        Collections.reverse(path);
        return path;
    }
}
