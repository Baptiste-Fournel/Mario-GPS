package application.algorithms;

import application.algorithms.DijkstraPathFinder.Coordinate;
import application.interfaces.PathFindingUseCase;
import domain.GameMap;

import java.util.*;

public class AStarPathFinder implements PathFindingUseCase {

    private static final List<Coordinate> DIRECTIONS = List.of(
            new Coordinate(0, -1),
            new Coordinate(0, 1),
            new Coordinate(-1, 0),
            new Coordinate(1, 0)
    );

    @Override
    public List<Coordinate> execute(GameMap map, Coordinate start, Coordinate goal) {
        Map<Coordinate, Integer> gScore = new HashMap<>();
        Map<Coordinate, Integer> fScore = new HashMap<>();
        Map<Coordinate, Coordinate> cameFrom = new HashMap<>();
        PriorityQueue<Coordinate> openSet = new PriorityQueue<>(Comparator.comparingInt(fScore::get));

        gScore.put(start, 0);
        fScore.put(start, heuristic(start, goal));
        openSet.add(start);

        while (!openSet.isEmpty()) {
            Coordinate current = openSet.poll();
            if (current.equals(goal)) return reconstructPath(cameFrom, goal);

            for (Coordinate dir : DIRECTIONS) {
                Coordinate neighbor = new Coordinate(current.x() + dir.x(), current.y() + dir.y());
                if (!isValid(map, neighbor, map.getWidth(), map.getHeight())) continue;

                int tentativeG = gScore.getOrDefault(current, Integer.MAX_VALUE) + 1;
                if (tentativeG < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeG);
                    fScore.put(neighbor, tentativeG + heuristic(neighbor, goal));
                    openSet.add(neighbor);
                }
            }
        }
        return null;
    }

    private int heuristic(Coordinate a, Coordinate b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
    }

    private boolean isValid(GameMap map, Coordinate c, int w, int h) {
        return c.x() >= 0 && c.x() < w && c.y() >= 0 && c.y() < h &&
                switch (map.getCell(c.x(), c.y()).getType()) {
                    case HERBE, ARRETE_HORIZONTAL, ARRETE_VERTICAL, NOEUD, START, CHATEAU -> true;
                    default -> false;
                };
    }

    private List<Coordinate> reconstructPath(Map<Coordinate, Coordinate> prev, Coordinate goal) {
        List<Coordinate> path = new ArrayList<>();
        for (Coordinate at = goal; at != null; at = prev.get(at)) path.add(at);
        Collections.reverse(path);
        return path;
    }
}
