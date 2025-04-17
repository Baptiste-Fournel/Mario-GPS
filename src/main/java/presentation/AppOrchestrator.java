package presentation;

import javafx.application.Application;
import javafx.stage.Stage;
import presentation.ui.MapGeneratorUI;

public class AppOrchestrator extends Application {

    @Override
    public void start(Stage primaryStage) {
        new MapGeneratorUI().start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
