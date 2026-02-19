package tn.esprit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFx extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage)  throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Landing.fxml"));
        Parent root = loader.load();
        stage.setTitle("StartupFlow Tunisia - Sign Up");

        stage.setScene(new Scene(root));
        stage.show();

    }
}
