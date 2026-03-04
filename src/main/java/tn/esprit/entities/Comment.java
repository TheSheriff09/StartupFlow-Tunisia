package tn.esprit.entities;

import java.time.LocalDateTime;

public class Comment {
    private int id;
    private String content;
    private LocalDateTime createdAt;
    private int postId; // foreign key to ForumPost
    private int userId; // foreign key to User
    private String authorName; // author name

    public Comment() {
    }

    public Comment(int id, String content, LocalDateTime createdAt, int postId, int userId, String authorName) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.postId = postId;
        this.userId = userId;
        this.authorName = authorName;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", postId=" + postId +
                ", userId=" + userId +
                ", authorName='" + authorName + '\'' +
                '}';
    }
}
