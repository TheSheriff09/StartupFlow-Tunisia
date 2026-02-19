package tn.esprit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainAdminFx extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ForumAdmin.fxml"));
        Parent root = loader.load();
        stage.setTitle("Forum Admin Panel");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
