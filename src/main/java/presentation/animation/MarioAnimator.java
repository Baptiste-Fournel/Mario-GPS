package presentation.animation;

import application.algorithms.DijkstraPathFinder.Coordinate;
import application.components.MapRenderer;
import application.components.PathInterpreter;
import domain.GameMap;
import domain.MapElementType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.util.Duration;
import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class MarioAnimator {

    private final GraphicsContext graphics;
    private final MapRenderer renderer;
    private final GameMap map;
    private final Image marioImage;
    @Getter
    private final Consumer<Coordinate> onMarioPositionUpdate;

    public MarioAnimator(GraphicsContext graphics, MapRenderer renderer, GameMap map, Image marioImage, Consumer<Coordinate> onMarioPositionUpdate) {
        this.graphics = graphics;
        this.renderer = renderer;
        this.map = map;
        this.marioImage = marioImage;
        this.onMarioPositionUpdate = onMarioPositionUpdate;
    }

    public void animate(List<Coordinate> path, Set<Coordinate> modifiedCells, Runnable onFinished) {
        Timeline timeline = new Timeline();

        for (int i = 1; i < path.size(); i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(Duration.millis(100 * i), e -> {
                Coordinate current = path.get(index);
                Coordinate previous = path.get(index - 1);

                if (index > 1) {
                    Coordinate before = path.get(index - 2);
                    MapElementType type = PathInterpreter.determineTileType(before, previous, current);
                    map.getCell(previous.x(), previous.y()).setType(type);
                    modifiedCells.add(new Coordinate(previous.x(), previous.y()));
                }

                onMarioPositionUpdate.accept(index < path.size() - 1 ? current : null);
                renderer.render();

                if (index < path.size() - 1) {
                    graphics.drawImage(marioImage,
                            current.x() * renderer.tileSize(),
                            current.y() * renderer.tileSize(),
                            renderer.tileSize(), renderer.tileSize());
                }
            });
            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.setOnFinished(e -> {
            renderer.render();
            onMarioPositionUpdate.accept(null);
            onFinished.run();
        });

        timeline.play();
    }

}