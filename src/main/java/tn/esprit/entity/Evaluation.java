package tn.esprit.entity;

public class Evaluation {
    private int id;
    private int fundingApplicationId;
    private int score;
    private String decision;
    private String evaluationComments;
    private int evaluatorId;
    private String riskLevel;
    private String fundingCategory;

    public Evaluation() {}

    public Evaluation(int id, int fundingApplicationId, int score, String decision,
                      String evaluationComments, int evaluatorId, String riskLevel,
                      String fundingCategory) {
        this.id = id;
        this.fundingApplicationId = fundingApplicationId;
        this.score = score;
        this.decision = decision;
        this.evaluationComments = evaluationComments;
        this.evaluatorId = evaluatorId;
        this.riskLevel = riskLevel;
        this.fundingCategory = fundingCategory;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFundingApplicationId() { return fundingApplicationId; }
    public void setFundingApplicationId(int fundingApplicationId) { this.fundingApplicationId = fundingApplicationId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getEvaluationComments() { return evaluationComments; }
    public void setEvaluationComments(String evaluationComments) { this.evaluationComments = evaluationComments; }

    public int getEvaluatorId() { return evaluatorId; }
    public void setEvaluatorId(int evaluatorId) { this.evaluatorId = evaluatorId; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getFundingCategory() { return fundingCategory; }
    public void setFundingCategory(String fundingCategory) { this.fundingCategory = fundingCategory; }

    @Override
    public String toString() {
        return "Evaluation{" +
                "id=" + id +
                ", fundingApplicationId=" + fundingApplicationId +
                ", score=" + score +
                ", decision='" + decision + '\'' +
                ", evaluationComments='" + evaluationComments + '\'' +
                ", evaluatorId=" + evaluatorId +
                ", riskLevel='" + riskLevel + '\'' +
                ", fundingCategory='" + fundingCategory + '\'' +
                '}';
    }
}
