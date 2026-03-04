package tn.esprit.GUI;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Comment;
import tn.esprit.entities.ForumPost;
import tn.esprit.entities.Interaction;
import tn.esprit.Services.CommentService;
import tn.esprit.Services.InteractionService;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PostItemController {

    @FXML
    private Label lblAuthor;
    @FXML
    private Label lblDate;
    @FXML
    private Label lblTitle;
    @FXML
    private Label lblContent;
    @FXML
    private ImageView imgPost;
    @FXML
    private Label lblLikes;

    @FXML
    private VBox commentsContainer;
    @FXML
    private VBox commentList;
    @FXML
    private TextArea taComment;
    @FXML
    private Button btnLike;
    @FXML
    private Button btnLove;
    @FXML
    private Button btnHaha;

    private ForumPost post;
    private final CommentService commentService = new CommentService();
    private int likeCount = 0; // Temporary in-memory counter for demo (should be in DB)

    private final InteractionService interactionService = new InteractionService();

    private int getCurrentUserId() {
        return (tn.esprit.utils.SessionManager.getUser() != null) ? tn.esprit.utils.SessionManager.getUser().getId()
                : 1;
    }

    private String getSessionUserName() {
        return (tn.esprit.utils.SessionManager.getUser() != null
                && tn.esprit.utils.SessionManager.getUser().getFullName() != null)
                        ? tn.esprit.utils.SessionManager.getUser().getFullName()
                        : "User";
    }

    public void setData(ForumPost post) {
        this.post = post;

        // Display the actual author name saved in the DB, or a fallback if null/empty
        String author = post.getAuthorName();
        if (author == null || author.trim().isEmpty()) {
            author = "Community User";
        }
        lblAuthor.setText(author);
        lblTitle.setText(post.getTitle());
        lblContent.setText(post.getContent());
        lblDate.setText(post.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")));

        updateLikeLabel();
        loadComments(); // Pre-load comments to ensure they exist, but container starts hidden

        // Image handling...
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()
                && !post.getImageUrl().equals("default_post.png")) {
            try {
                File file = new File(post.getImageUrl());
                if (file.exists()) {
                    imgPost.setImage(new Image(file.toURI().toString()));
                } else if (post.getImageUrl().startsWith("http")) {
                    imgPost.setImage(new Image(post.getImageUrl()));
                } else {
                    imgPost.setManaged(false);
                    imgPost.setVisible(false);
                }
            } catch (Exception e) {
                imgPost.setManaged(false);
                imgPost.setVisible(false);
            }
        } else {
            imgPost.setManaged(false);
            imgPost.setVisible(false);
        }
    }

    private void updateLikeLabel() {
        int count = interactionService.getInteractionCount(post.getId());
        lblLikes.setText(count + " Interactions");
    }

    // Renamed from onReact and updated
    @FXML
    private void likePost() {
        Interaction i = new Interaction(post.getId(), getCurrentUserId(), "LIKE");
        interactionService.add(i);
        updateLikeLabel(); // Changed from updateLikeCount() to updateLikeLabel()
        resetReactionStyles();
        btnLike.getStyleClass().add("reaction-active");
    }

    // Renamed from onLove and updated
    @FXML
    private void lovePost() {
        Interaction i = new Interaction(post.getId(), getCurrentUserId(), "LOVE");
        interactionService.add(i);
        updateLikeLabel(); // Changed from updateLikeCount() to updateLikeLabel()
        resetReactionStyles();
        btnLove.getStyleClass().add("reaction-active");
    }

    // Renamed from onHaha and updated
    @FXML
    private void hahaPost() {
        Interaction i = new Interaction(post.getId(), getCurrentUserId(), "HAHA");
        interactionService.add(i);
        updateLikeLabel(); // Changed from updateLikeCount() to updateLikeLabel()
        resetReactionStyles();
        btnHaha.getStyleClass().add("reaction-active");
    }

    private void resetReactionStyles() {
        btnLike.getStyleClass().remove("reaction-active");
        btnLove.getStyleClass().remove("reaction-active");
        btnHaha.getStyleClass().remove("reaction-active");
    }

    @FXML
    private void onComment() {
        boolean isVisible = commentsContainer.isVisible();
        commentsContainer.setVisible(!isVisible);
        commentsContainer.setManaged(!isVisible);

        if (!isVisible) {
            loadComments();
        }
    }

    private void loadComments() {
        commentList.getChildren().clear();
        List<Comment> comments = commentService.getByPostId(post.getId());
        for (Comment c : comments) {
            VBox bubble = new VBox(3);
            bubble.getStyleClass().add("comment-box");

            String author = c.getAuthorName() != null ? c.getAuthorName() : "User";
            Label userLbl = new Label(author);
            userLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #7b61ff;");

            Label contentLbl = new Label(c.getContent());
            contentLbl.setWrapText(true);
            contentLbl.setStyle("-fx-text-fill: #222; -fx-font-size: 13px;");

            bubble.getChildren().addAll(userLbl, contentLbl);
            commentList.getChildren().add(bubble);
        }
    }

    @FXML
    private void submitComment() {
        String content = taComment.getText().trim();
        if (content.isEmpty()) { // Corrected the if condition structure
            return;
        }

        Comment c = new Comment();
        c.setPostId(post.getId());
        c.setUserId(getCurrentUserId());
        c.setAuthorName(getSessionUserName());
        c.setContent(content);
        c.setCreatedAt(LocalDateTime.now());

        commentService.add(c);
        taComment.clear();
        loadComments();
    }
}
