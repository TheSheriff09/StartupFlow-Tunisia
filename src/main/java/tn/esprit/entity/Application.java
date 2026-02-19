package tn.esprit.entity;

public class Application {
    private int id;
    private int entrepreneurId;
    private float amount;
    private String status;
    private String submissionDate;
    private String applicationReason;
    private int projectId;
    private String paymentSchedule;
    private String attachment;

    // Constructors
    public Application() {}

    public Application(int id, int entrepreneurId, float amount, String status,
                       String submissionDate, String applicationReason,
                       int projectId, String paymentSchedule, String attachment) {
        this.id = id;
        this.entrepreneurId = entrepreneurId;
        this.amount = amount;
        this.status = status;
        this.submissionDate = submissionDate;
        this.applicationReason = applicationReason;
        this.projectId = projectId;
        this.paymentSchedule = paymentSchedule;
        this.attachment = attachment;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEntrepreneurId() { return entrepreneurId; }
    public void setEntrepreneurId(int entrepreneurId) { this.entrepreneurId = entrepreneurId; }

    public float getAmount() { return amount; }
    public void setAmount(float amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(String submissionDate) { this.submissionDate = submissionDate; }

    public String getApplicationReason() { return applicationReason; }
    public void setApplicationReason(String applicationReason) { this.applicationReason = applicationReason; }

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public String getPaymentSchedule() { return paymentSchedule; }
    public void setPaymentSchedule(String paymentSchedule) { this.paymentSchedule = paymentSchedule; }

    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }

    @Override
    public String toString() {
        return "Application{" +
                "id=" + id +
                ", entrepreneurId=" + entrepreneurId +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", submissionDate='" + submissionDate + '\'' +
                ", applicationReason='" + applicationReason + '\'' +
                ", projectId=" + projectId +
                ", paymentSchedule='" + paymentSchedule + '\'' +
                ", attachment='" + attachment + '\'' +
                '}';
    }
}
