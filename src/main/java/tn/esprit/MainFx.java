package tn.esprit;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class MainFx extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {


        // Simple landing page to choose role
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f0f2f5; -fx-padding: 50;");

        Label title = new Label("StartupFlow Forum");
        title.setFont(new Font("Arial Bold", 24));
        title.setStyle("-fx-text-fill: #333;");

        Button btnAdmin = new Button("Open Admin Panel");
        btnAdmin.setStyle(
                "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-cursor: hand;");
        btnAdmin.setOnAction(e -> loadView(stage, "/ForumAdmin.fxml", "Forum Admin Panel"));

        Button btnUser = new Button("Open User Feed");
        btnUser.setStyle(
                "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-cursor: hand;");
        btnUser.setOnAction(e -> loadView(stage, "/ForumFeed.fxml", "StartupFlow Forum"));

        root.getChildren().addAll(title, btnUser, btnAdmin); // User first as it's the main feature

        Scene scene = new Scene(root, 400, 300);
        stage.setTitle("StartupFlow Launcher");
        stage.setScene(scene);
        stage.show();
    }

    private void loadView(Stage stage, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
