package tn.esprit.entities;

public class MentorRecoRow {
    private int id;
    private String fullName;
    private String expertise;

    private double ratingPercent;     // ✅ from ML script
    private int reclamations90d;
    private String risk;
    private boolean best;

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getExpertise() { return expertise; }

    public double getRatingPercent() { return ratingPercent; }   // ✅
    public int getReclamations90d() { return reclamations90d; }
    public String getRisk() { return risk; }
    public boolean isBest() { return best; }
}