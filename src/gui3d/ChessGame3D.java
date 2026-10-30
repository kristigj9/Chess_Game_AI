package gui3d;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChessGame3D extends Application {

    @Override
    public void start(Stage stage) {
        Game3DView view = new Game3DView();
        Scene scene = view.createScene();

        stage.setTitle("Chess Game 3D");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        view.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}