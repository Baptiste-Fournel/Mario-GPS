package application;

import application.interfaces.PathFindingUseCase;
import domain.GameMap;
import domain.MapCell;

import java.util.*;

public class ShortestPathUseCase implements PathFindingUseCase {

    public record Coordinate(int x, int y) {
    }

    private static final List<Coordinate> DIRECTIONS = List.of(
            new Coordinate(0, -1),
            new Coordinate(0, 1),
            new Coordinate(-1, 0),
            new Coordinate(1, 0)
    );

    @Override
    public List<Coordinate> execute(GameMap map, Coordinate start, Coordinate goal) {
        int mapWidth = map.getWidth();
        int mapHeight = map.getHeight();

        Map<Coordinate, Coordinate> previousStep = new HashMap<>();
        Map<Coordinate, Integer> shortestDistances = new HashMap<>();
        PriorityQueue<Coordinate> toVisit = new PriorityQueue<>(Comparator.comparingInt(shortestDistances::get));

        shortestDistances.put(start, 0);
        toVisit.add(start);

        while (!toVisit.isEmpty()) {
            Coordinate current = toVisit.poll();

            if (current.equals(goal)) {
                return buildPath(previousStep, goal);
            }

            for (Coordinate direction : DIRECTIONS) {
                Coordinate neighbor = new Coordinate(current.x + direction.x, current.y + direction.y);

                if (!isWithinBounds(neighbor, mapWidth, mapHeight)) continue;
                if (!isWalkable(map.getCell(neighbor.x, neighbor.y))) continue;

                int tentativeDistance = shortestDistances.get(current) + 1;

                if (tentativeDistance < shortestDistances.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    shortestDistances.put(neighbor, tentativeDistance);
                    previousStep.put(neighbor, current);
                    toVisit.add(neighbor);
                }
            }
        }

        return null;
    }

    private boolean isWithinBounds(Coordinate coord, int width, int height) {
        return coord.x >= 0 && coord.x < width && coord.y >= 0 && coord.y < height;
    }

    private boolean isWalkable(MapCell cell) {
        return switch (cell.getType()) {
            case HERBE, NOEUD, ARRETE_HORIZONTAL, ARRETE_VERTICAL, START, CHATEAU -> true;
            default -> false;
        };
    }

    private List<Coordinate> buildPath(Map<Coordinate, Coordinate> previousStep, Coordinate target) {
        List<Coordinate> path = new ArrayList<>();
        Coordinate current = target;

        while (previousStep.containsKey(current)) {
            path.add(current);
            current = previousStep.get(current);
        }

        path.add(current);

        Collections.reverse(path);
        return path;
    }

}