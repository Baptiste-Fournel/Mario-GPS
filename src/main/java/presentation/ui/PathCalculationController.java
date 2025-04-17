package presentation.ui;

import application.algorithms.AStarPathFinder;
import application.algorithms.DijkstraPathFinder;
import application.algorithms.DijkstraPathFinder.Coordinate;
import application.enums.PathAlgorithm;
import application.interfaces.PathFindingUseCase;
import components.MapRenderer;
import domain.GameMap;
import javafx.scene.control.Label;
import lombok.Getter;
import presentation.animation.MarioAnimator;

import java.util.*;

public class PathCalculationController {

    private final GameMap map;
    private final MapRenderer renderer;
    private final MapInteractionHandler interactionHandler;

    @Getter
    private final List<Coordinate> currentPath = new ArrayList<>();

    private final Set<Coordinate> tilesModifiedByAnimation = new HashSet<>();

    @Getter
    private PathAlgorithm currentAlgorithm = PathAlgorithm.DIJKSTRA;

    public PathCalculationController(GameMap map, MapRenderer renderer, MapInteractionHandler handler) {
        this.map = map;
        this.renderer = renderer;
        this.interactionHandler = handler;
    }

    public void setAlgorithm(PathAlgorithm algo) {
        this.currentAlgorithm = algo;
    }

    public Set<Coordinate> getModifiedCells() {
        return tilesModifiedByAnimation;
    }

    public void calculateAndAnimate(MarioAnimator animator, Label timerLabel) {
        Coordinate start = interactionHandler.getStartCoordinate();
        Coordinate end = interactionHandler.getEndCoordinate();

        if (start == null || end == null) return;

        PathFindingUseCase algo = switch (currentAlgorithm) {
            case DIJKSTRA -> new DijkstraPathFinder();
            case ASTAR -> new AStarPathFinder();
        };

        long startTime = System.nanoTime();
        List<Coordinate> path = algo.execute(map, start, end);
        long endTime = System.nanoTime();

        double elapsedMs = (endTime - startTime) / 1_000_000.0;
        timerLabel.setText("Temps d'exécution : " + String.format("%.2f", elapsedMs) + " ms");

        if (path == null || path.isEmpty()) return;

        currentPath.clear();
        currentPath.addAll(path);
        tilesModifiedByAnimation.clear();

        animator.animate(currentPath, tilesModifiedByAnimation, () -> {
            renderer.render();
            animator.getOnMarioPositionUpdate().accept(null);
        });
    }
}