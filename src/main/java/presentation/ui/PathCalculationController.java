package presentation.ui;

import application.algorithms.AStarPathFinder;
import application.algorithms.DijkstraPathFinder;
import application.components.MapRenderer;
import application.enums.PathAlgorithm;
import application.interfaces.PathFindingUseCase;
import domain.Coordinate;
import domain.GameMap;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import lombok.Getter;
import presentation.animation.MarioAnimator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PathCalculationController {

    @Getter
    private final List<Coordinate> currentPath = new ArrayList<>();

    @Getter
    private final Set<Coordinate> modifiedCells = new HashSet<>();

    private GameMap map;
    private MapRenderer renderer;
    private final MapInteractionHandler interactionHandler;

    @Getter
    private PathAlgorithm currentAlgorithm = PathAlgorithm.DIJKSTRA;

    public PathCalculationController(GameMap map, MapRenderer renderer, MapInteractionHandler handler) {
        this.map = map;
        this.renderer = renderer;
        this.interactionHandler = handler;
    }

    public void setAlgorithm(PathAlgorithm algo) {
        currentPath.clear();
        modifiedCells.clear();
        this.currentAlgorithm = algo;
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

        if (path == null || path.isEmpty()) {
            showErrorWhenNoPathAvailable();
            return;
        }

        currentPath.clear();
        currentPath.addAll(path);
        modifiedCells.clear();

        animator.animate(currentPath, modifiedCells, () -> {
            renderer.render();
            animator.getOnMarioPositionUpdate().accept(null);
        });
    }

    private static void showErrorWhenNoPathAvailable() {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Chemin introuvable");
            alert.setHeaderText(null);
            alert.setContentText("Aucun chemin n'est possible entre le point de départ et d’arrivée.");
            alert.showAndWait();
        });
    }

    public void updateMap(GameMap newMap) {
        this.map = newMap;
    }

    public void updateRenderer(MapRenderer newRenderer) {
        this.renderer = newRenderer;
    }
}