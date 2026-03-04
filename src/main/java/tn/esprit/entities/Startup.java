package tn.esprit.entities;

import java.time.LocalDate;

/**
 * Maps to the `startup` table in the startupflow database.
 */
public class Startup {

    private int startupID;
    private String name;
    private String description;
    private String sector;
    private String imageURL;
    private LocalDate creationDate;
    private Double kpiScore;
    private LocalDate lastEvaluationDate;
    private String stage;
    private String status;
    private Integer mentorID;
    private Double fundingAmount;
    private String incubatorProgram;
    private Integer founderID;
    private Integer businessPlanID;

    // Foreign key to User entity
    private int userId;

    // ── Constructors ──────────────────────────────────────────

    public Startup() {
    }

    // Legacy constructor from main project
    public Startup(int startupID, String name, String description, String sector) {
        this.startupID = startupID;
        this.name = name;
        this.description = description;
        this.sector = sector;
    }

    public Startup(int startupID, String name, String description, String sector,
            String imageURL, LocalDate creationDate, Double kpiScore,
            LocalDate lastEvaluationDate, String stage, String status,
            Integer mentorID, Double fundingAmount, String incubatorProgram,
            Integer founderID, Integer businessPlanID, int userId) {
        this.startupID = startupID;
        this.name = name;
        this.description = description;
        this.sector = sector;
        this.imageURL = imageURL;
        this.creationDate = creationDate;
        this.kpiScore = kpiScore;
        this.lastEvaluationDate = lastEvaluationDate;
        this.stage = stage;
        this.status = status;
        this.mentorID = mentorID;
        this.fundingAmount = fundingAmount;
        this.incubatorProgram = incubatorProgram;
        this.founderID = founderID;
        this.businessPlanID = businessPlanID;
        this.userId = userId;
    }

    // Constructor without userId for backward compatibility with old module code
    public Startup(int startupID, String name, String description, String sector,
            String imageURL, LocalDate creationDate, Double kpiScore,
            LocalDate lastEvaluationDate, String stage, String status,
            Integer mentorID, Double fundingAmount, String incubatorProgram,
            Integer founderID, Integer businessPlanID) {
        this(startupID, name, description, sector, imageURL, creationDate, kpiScore,
                lastEvaluationDate, stage, status, mentorID, fundingAmount, incubatorProgram,
                founderID, businessPlanID, 0);
    }

    // ── Getters / Setters ─────────────────────────────────────

    public int getStartupID() {
        return startupID;
    }

    public void setStartupID(int startupID) {
        this.startupID = startupID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public Double getKpiScore() {
        return kpiScore;
    }

    public void setKpiScore(Double kpiScore) {
        this.kpiScore = kpiScore;
    }

    public LocalDate getLastEvaluationDate() {
        return lastEvaluationDate;
    }

    public void setLastEvaluationDate(LocalDate d) {
        this.lastEvaluationDate = d;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getMentorID() {
        return mentorID;
    }

    public void setMentorID(Integer mentorID) {
        this.mentorID = mentorID;
    }

    public Double getFundingAmount() {
        return fundingAmount;
    }

    public void setFundingAmount(Double fundingAmount) {
        this.fundingAmount = fundingAmount;
    }

    public String getIncubatorProgram() {
        return incubatorProgram;
    }

    public void setIncubatorProgram(String p) {
        this.incubatorProgram = p;
    }

    public Integer getFounderID() {
        return founderID;
    }

    public void setFounderID(Integer founderID) {
        this.founderID = founderID;
    }

    public Integer getBusinessPlanID() {
        return businessPlanID;
    }

    public void setBusinessPlanID(Integer businessPlanID) {
        this.businessPlanID = businessPlanID;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Startup{id=" + startupID + ", name='" + name + "', sector='" + sector + "', userId=" + userId + "}";
    }
}