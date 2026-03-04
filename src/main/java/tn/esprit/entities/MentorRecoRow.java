package tn.esprit.entities;

public class MentorRecoRow {
    private int id;
    private String fullName;
    private String expertise;

    private double ratingPercent;  // from ML; < 0 means insufficient data (show N/A)
    private int reclamations90d;
    private boolean best;

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getExpertise() { return expertise; }

    public double getRatingPercent() { return ratingPercent; }
    public int getReclamations90d() { return reclamations90d; }
    public boolean isBest() { return best; }
}