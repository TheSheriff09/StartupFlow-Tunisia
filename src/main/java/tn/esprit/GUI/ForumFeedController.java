package tn.esprit.GUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.ForumPost;
import tn.esprit.services.ForumPostService;

import java.io.IOException;
import java.util.List;

public class ForumFeedController {

    @FXML
    private VBox postsContainer;

    private final ForumPostService postService = new ForumPostService();

    @FXML
    public void initialize() {
        refreshFeed();
    }

    public void refreshFeed() {
        postsContainer.getChildren().clear();
        List<ForumPost> posts = postService.list();

        // Sort by newest first
        posts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));

        for (ForumPost post : posts) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PostItem.fxml"));
                Parent postItem = loader.load();

                PostItemController controller = loader.getController();
                controller.setData(post);

                postsContainer.getChildren().add(postItem);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void addPost() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddPost.fxml"));
            Parent root = loader.load();

            AddPostController controller = loader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Create Post");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Navigation (Placeholders or actual implementations)
    @FXML
    private void goDashboard() {
        System.out.println("Dashboard");
    }

    @FXML
    private void goStartup() {
        System.out.println("Startup");
    }

    @FXML
    private void goMentorship() {
        System.out.println("Mentorship");
    }

    @FXML
    private void goFunding() {
        System.out.println("Funding");
    }

    @FXML
    private void goForum() {
        /* Already here */ }

    @FXML
    private void logout() {
        try {
            // Return to MainFx (Role Selection)
            tn.esprit.MainFx main = new tn.esprit.MainFx();
            main.start((javafx.stage.Stage) postsContainer.getScene().getWindow());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
