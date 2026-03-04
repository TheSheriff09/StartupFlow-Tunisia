package tn.esprit.entities;

import java.time.LocalDate;

/**
 * Maps to the `businessplan` table in the startupflow database.
 * Column names match exactly: businessPlanID, marketAnalysis, valueProposition,
 * etc.
 */
public class BusinessPlan {

    private int businessPlanID;
    private String title;
    private String description;
    private String marketAnalysis;
    private String valueProposition;
    private String businessModel;
    private String marketingStrategy;
    private String financialForecast;
    private Double fundingRequired;
    private String timeline;
    private String status;
    private LocalDate creationDate;
    private LocalDate lastUpdate;
    private int startupID;

    // Foreign key to User entity
    private int userId;

    // ── Constructors ──────────────────────────────────────────

    public BusinessPlan() {
    }

    public BusinessPlan(int businessPlanID, String title, String description,
            String marketAnalysis, String valueProposition,
            String businessModel, String marketingStrategy,
            String financialForecast, Double fundingRequired,
            String timeline, String status,
            LocalDate creationDate, LocalDate lastUpdate,
            int startupID, int userId) {
        this.businessPlanID = businessPlanID;
        this.title = title;
        this.description = description;
        this.marketAnalysis = marketAnalysis;
        this.valueProposition = valueProposition;
        this.businessModel = businessModel;
        this.marketingStrategy = marketingStrategy;
        this.financialForecast = financialForecast;
        this.fundingRequired = fundingRequired;
        this.timeline = timeline;
        this.status = status;
        this.creationDate = creationDate;
        this.lastUpdate = lastUpdate;
        this.startupID = startupID;
        this.userId = userId;
    }

    // Constructor without userId for backward compatibility
    public BusinessPlan(int businessPlanID, String title, String description,
            String marketAnalysis, String valueProposition,
            String businessModel, String marketingStrategy,
            String financialForecast, Double fundingRequired,
            String timeline, String status,
            LocalDate creationDate, LocalDate lastUpdate,
            int startupID) {
        this(businessPlanID, title, description, marketAnalysis, valueProposition,
                businessModel, marketingStrategy, financialForecast, fundingRequired,
                timeline, status, creationDate, lastUpdate, startupID, 0);
    }

    // ── Getters / Setters ─────────────────────────────────────

    public int getBusinessPlanID() {
        return businessPlanID;
    }

    public void setBusinessPlanID(int id) {
        this.businessPlanID = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMarketAnalysis() {
        return marketAnalysis;
    }

    public void setMarketAnalysis(String marketAnalysis) {
        this.marketAnalysis = marketAnalysis;
    }

    public String getValueProposition() {
        return valueProposition;
    }

    public void setValueProposition(String valueProposition) {
        this.valueProposition = valueProposition;
    }

    public String getBusinessModel() {
        return businessModel;
    }

    public void setBusinessModel(String businessModel) {
        this.businessModel = businessModel;
    }

    public String getMarketingStrategy() {
        return marketingStrategy;
    }

    public void setMarketingStrategy(String marketingStrategy) {
        this.marketingStrategy = marketingStrategy;
    }

    public String getFinancialForecast() {
        return financialForecast;
    }

    public void setFinancialForecast(String financialForecast) {
        this.financialForecast = financialForecast;
    }

    public Double getFundingRequired() {
        return fundingRequired;
    }

    public void setFundingRequired(Double fundingRequired) {
        this.fundingRequired = fundingRequired;
    }

    public String getTimeline() {
        return timeline;
    }

    public void setTimeline(String timeline) {
        this.timeline = timeline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDate getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDate lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getStartupID() {
        return startupID;
    }

    public void setStartupID(int startupID) {
        this.startupID = startupID;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "BusinessPlan{id=" + businessPlanID + ", title='" + title + "', status='" + status + "', userId="
                + userId + "}";
    }
}
