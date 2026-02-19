package tn.esprit.entities;

import java.sql.Timestamp;

public class User {

    private int id;
    private String fullName;
    private String email;
    private String passwordHash;
    private String role;
    private String status;

    private String mentorExpertise;
    private String evaluatorLevel;

    private Timestamp createdAt;

    public User() {}


    public User(String fullName, String email, String passwordHash, String role, String status,
                String mentorExpertise, String evaluatorLevel) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.mentorExpertise = mentorExpertise;
        this.evaluatorLevel = evaluatorLevel;
    }

    public User(int id, String fullName, String email, String passwordHash,
                String role, String status, String mentorExpertise, String evaluatorLevel,
                Timestamp createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.mentorExpertise = mentorExpertise;
        this.evaluatorLevel = evaluatorLevel;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMentorExpertise() { return mentorExpertise; }
    public void setMentorExpertise(String mentorExpertise) { this.mentorExpertise = mentorExpertise; }

    public String getEvaluatorLevel() { return evaluatorLevel; }
    public void setEvaluatorLevel(String evaluatorLevel) { this.evaluatorLevel = evaluatorLevel; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                ", mentorExpertise='" + mentorExpertise + '\'' +
                ", evaluatorLevel='" + evaluatorLevel + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}