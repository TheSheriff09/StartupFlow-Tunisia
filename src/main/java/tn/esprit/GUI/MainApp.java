package tn.esprit.GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.utils.ThemeManager;

/**
 * Application entry point.
 * Loads the Startup card view as the main screen.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/startupview.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1100, 720);
        ThemeManager.getInstance().applyTo(scene);
        primaryStage.setTitle("StartupFlow — Startup Management");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(560);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

