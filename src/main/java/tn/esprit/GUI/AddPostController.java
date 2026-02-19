package tn.esprit.GUI;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.ForumPost;
import tn.esprit.services.ForumPostService;

import java.io.File;
import java.time.LocalDateTime;

public class AddPostController {

    @FXML
    private TextField tfTitle;
    @FXML
    private TextArea taContent;
    @FXML
    private Label lblImageName;
    @FXML
    private ImageView imgPreview;

    private File selectedImageFile;
    private ForumFeedController parentController;
    private final ForumPostService postService = new ForumPostService();

    public void setParentController(ForumFeedController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        selectedImageFile = fileChooser.showOpenDialog(tfTitle.getScene().getWindow());

        if (selectedImageFile != null) {
            lblImageName.setText(selectedImageFile.getName());
            imgPreview.setImage(new Image(selectedImageFile.toURI().toString()));
        }
    }

    @FXML
    private void submit() {
        String title = tfTitle.getText().trim();
        String content = taContent.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            showAlert("Validation Error", "Title and Content are required.");
            return;
        }

        // Profanity Check
        if (tn.esprit.utils.ProfanityFilter.containsProfanity(title) ||
                tn.esprit.utils.ProfanityFilter.containsProfanity(content)) {
            showAlert("Content Warning", "Your post contains inappropriate language. Please revise.");
            return;
        }

        ForumPost post = new ForumPost();
        post.setTitle(title);
        post.setContent(content);
        post.setCreatedAt(LocalDateTime.now());

        if (selectedImageFile != null) {
            post.setImageUrl(selectedImageFile.getAbsolutePath());
        }

        postService.add(post);

        if (parentController != null) {
            parentController.refreshFeed();
        }

        close();
    }

    @FXML
    private void cancel() {
        close();
    }

    private void close() {
        ((Stage) tfTitle.getScene().getWindow()).close();
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
