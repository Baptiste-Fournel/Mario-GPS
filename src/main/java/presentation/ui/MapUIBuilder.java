package presentation.ui;

import application.enums.PathAlgorithm;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.stream.IntStream;

public class MapUIBuilder {

    private final Button startButton;
    private final Button endButton;
    private final Button calculateButton;
    private final Button resetButton;
    private final ComboBox<PathAlgorithm> algoSelector;
    private final ComboBox<Integer> widthSelector;
    private final ComboBox<Integer> heightSelector;
    private final Label timerLabel;

    private final MapInteractionHandler interactionHandler;
    private final PathCalculationController pathController;
    private final Canvas canvas;
    private final MapGeneratorUI mapGeneratorUI;

    public MapUIBuilder(Canvas canvas, MapInteractionHandler handler, PathCalculationController controller, MapGeneratorUI mapGeneratorUI) {
        this.canvas = canvas;
        this.interactionHandler = handler;
        this.pathController = controller;
        this.mapGeneratorUI = mapGeneratorUI;

        startButton = createStyledButton("Point de départ");
        endButton = createStyledButton("Point d’arrivée");
        calculateButton = createStyledButton("Calculer le chemin");
        resetButton = createStyledButton("Réinitialiser");

        timerLabel = new Label("Temps d'exécution : ");
        timerLabel.getStyleClass().add("timer-label");

        algoSelector = new ComboBox<>();
        algoSelector.getItems().addAll(PathAlgorithm.values());
        algoSelector.setValue(pathController.getCurrentAlgorithm());
        algoSelector.getStyleClass().add("algo-select");

        widthSelector = new ComboBox<>();
        heightSelector = new ComboBox<>();
        IntStream.rangeClosed(5, 100).forEach(i -> {
            widthSelector.getItems().add(i);
            heightSelector.getItems().add(i);
        });
        widthSelector.setValue(20);
        heightSelector.setValue(20);
        widthSelector.setPromptText("Largeur");
        heightSelector.setPromptText("Hauteur");

        setActions();
    }

    private void setActions() {
        startButton.setOnAction(e -> {
            interactionHandler.setSelectingStart(true);
            interactionHandler.setSelectingEnd(false);
            startButton.getStyleClass().add("active");
            endButton.getStyleClass().remove("active");
            canvas.setCursor(Cursor.CROSSHAIR);
        });

        endButton.setOnAction(e -> {
            interactionHandler.setSelectingStart(false);
            interactionHandler.setSelectingEnd(true);
            endButton.getStyleClass().add("active");
            startButton.getStyleClass().remove("active");
            canvas.setCursor(Cursor.CROSSHAIR);
        });

        calculateButton.setOnAction(e -> {
            mapGeneratorUI.clearCurrentPath();
            pathController.calculateAndAnimate(
                    mapGeneratorUI.getMarioAnimator(), timerLabel
            );
        });

        resetButton.setOnAction(e -> {
            int width = widthSelector.getValue();
            int height = heightSelector.getValue();
            mapGeneratorUI.regenerateMap(width, height);
            interactionHandler.reset();
            pathController.getCurrentPath().clear();
            pathController.getModifiedCells().clear();
            timerLabel.setText("Temps d'exécution : ");
        });

        algoSelector.setOnAction(e -> {
            mapGeneratorUI.clearCurrentPath();
            pathController.setAlgorithm(algoSelector.getValue());
            mapGeneratorUI.getRenderer().render();
            mapGeneratorUI.drawSpecialImages(null);
        });
    }

    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("button");
        return btn;
    }

    public HBox build() {
        VBox controlPanel = new VBox(20, startButton, endButton, calculateButton, resetButton, algoSelector, widthSelector, heightSelector, timerLabel);
        controlPanel.setAlignment(Pos.TOP_CENTER);
        controlPanel.setPadding(new Insets(20));

        StackPane canvasContainer = new StackPane(canvas);
        canvasContainer.setPadding(new Insets(20));

        HBox layout = new HBox(30, canvasContainer, controlPanel);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);
        return layout;
    }

    public boolean isSelectingStart() {
        return interactionHandler.isSelectingStart();
    }

    public boolean isSelectingEnd() {
        return interactionHandler.isSelectingEnd();
    }
}