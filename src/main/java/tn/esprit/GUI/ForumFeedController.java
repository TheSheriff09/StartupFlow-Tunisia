package tn.esprit.GUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.ForumPost;
import tn.esprit.Services.ForumPostService;

import java.io.IOException;
import java.util.List;

import javafx.animation.TranslateTransition;
import javafx.animation.Animation;
import javafx.util.Duration;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.NavContext;
import tn.esprit.utils.SessionManager;
import tn.esprit.utils.TopBarHelper;

public class ForumFeedController {

    @FXML
    private VBox postsContainer;

    @FXML
    private Label newsTickerLabel;

    @FXML
    private HBox newsTickerContainer;

    @FXML
    private javafx.scene.layout.Pane tickerPane;

    @FXML
    private Label navForumDashboard;

    @FXML
    private Label navUserManagement;

    @FXML
    private MenuButton userMenuBtn;

    private final ForumPostService postService = new ForumPostService();

    @FXML
    public void initialize() {
        // ── Session guard ──
        if (!SessionManager.requireLogin(postsContainer))
            return;

        // ── Top bar setup (reusable) ──
        TopBarHelper.setup(userMenuBtn, null, postsContainer);

        refreshFeed();
        initNewsTicker();
        setupAdminNav();
    }

    private void setupAdminNav() {
        if (navForumDashboard == null)
            return;

        boolean isAdmin = SessionManager.isAdmin();

        navForumDashboard.setVisible(isAdmin);
        navForumDashboard.setManaged(isAdmin);

        if (navUserManagement != null) {
            navUserManagement.setVisible(isAdmin);
            navUserManagement.setManaged(isAdmin);
        }
    }

    private void initNewsTicker() {
        ForumPost newestPost = postService.getNewestPost();
        if (newestPost != null) {
            String content = newestPost.getContent();
            if (content != null && content.length() > 50) {
                content = content.substring(0, 50) + "...";
            }

            newsTickerLabel.setText("BREAKING: " + newestPost.getTitle() + " - " + content + " ("
                    + newestPost.getCreatedAt().toString() + ")");

            Platform.runLater(() -> {
                if (tickerPane != null) {
                    javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
                    clip.widthProperty().bind(tickerPane.widthProperty());
                    clip.heightProperty().bind(tickerPane.heightProperty());
                    tickerPane.setClip(clip);
                }

                double containerWidth = tickerPane != null ? tickerPane.getWidth() : newsTickerContainer.getWidth();
                double labelWidth = newsTickerLabel.getWidth();

                TranslateTransition transition = new TranslateTransition(Duration.seconds(15), newsTickerLabel);
                transition.setFromX(containerWidth);
                transition.setToX(-labelWidth - 50);
                transition.setCycleCount(Animation.INDEFINITE);
                transition.play();
            });
        } else {
            newsTickerLabel.setText("No news available.");
        }
    }

    public void refreshFeed() {
        postsContainer.getChildren().clear();
        List<ForumPost> posts = postService.list();

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

    // ── Navigation ──

    @FXML
    private void onDashboard() {
        NavigationManager.goToDashboard(postsContainer);
    }

    @FXML
    private void onStartup() {
        NavigationManager.navigateTo(postsContainer, "/startupview.fxml");
    }

    @FXML
    private void onMentorship() {
        System.out.println("Navigating to Mentorship");
    }

    @FXML
    private void onFunding() {
        System.out.println("Navigating to Funding");
    }

    @FXML
    private void onFourm() {
        /* Already on Forum */
    }

    @FXML
    private void onForumDashboard() {
        tn.esprit.GUI.ForumAdminController.showAnalyticsOnLoad = true;
        NavigationManager.navigateTo(postsContainer, "/ForumAdmin.fxml");
    }

    @FXML
    private void onUserManagement() {
        NavigationManager.navigateTo(postsContainer, "/UserManagement.fxml");
    }

    @FXML
    private void onManageProfile() {
        NavContext.setBack("/ForumFeed.fxml");
        NavigationManager.navigateTo(postsContainer, "/ManageProfile.fxml");
    }

    @FXML
    private void onStartups() {
        NavigationManager.navigateTo(postsContainer, "/startupview.fxml");
    }

    @FXML
    private void onSettings() {
        System.out.println("Settings");
    }

    @FXML
    private void onLogout() {
        NavigationManager.logout(postsContainer);
    }
}
