package tn.esprit.GUI.startup;

import com.google.zxing.WriterException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.BusinessPlan;
import tn.esprit.entities.Startup;
import tn.esprit.GUI.popup.DialogStyler;
import tn.esprit.Services.*;
import tn.esprit.utils.AlertUtil;
import tn.esprit.utils.ThemeManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import tn.esprit.Services.StartupWebServer;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * StartupDetailController — Full detail page shown after QR scan or 
 * direct navigation.  Displays startup info, business plans,
 * investment score, risk band and recommendation.
 */
public class StartupDetailController implements Initializable {

    @FXML private BorderPane mainContent;
    @FXML private StackPane  modalLayer;
    @FXML private Label      lblTitle;
    @FXML private Label      lblScore;
    @FXML private Label      lblRisk;
    @FXML private Label      lblRecommendation;
    @FXML private FlowPane   detailCards;
    @FXML private VBox       plansBox;
    @FXML private VBox       contentBox;
    @FXML private Button     themeToggle;

    private Startup              startup;
    private List<BusinessPlan>   plans;
    private double               investmentScore;
    private final StartupService     startupService = new StartupService();
    private final BusinessPlanService bpService      = new BusinessPlanService();

    @Override
    public void initialize(URL location, ResourceBundle resources) { }

    /**
     * Injects the startup to display and populates all sections.
     */
    public void initWithStartup(Startup s) {
        this.startup = s;
        this.plans   = bpService.getByStartup(s.getStartupID());
        this.investmentScore = startupService.calculateInvestmentScore(s.getStartupID());

        lblTitle.setText("📊  " + s.getName());
        // Set theme toggle text
        if (themeToggle != null)
            themeToggle.setText(ThemeManager.getInstance().isDark() ? "☀" : "🌙");
        populateScore();
        populateDetails();
        populatePlans();
    }

    /**
     * Loads a startup by ID (used after QR scan).
     * @return true if the startup was found, false otherwise
     */
    public boolean initWithStartupID(int startupID) {
        Startup s = startupService.getById(startupID);
        if (s == null) return false;
        initWithStartup(s);
        return true;
    }

    @FXML
    private void toggleTheme() {
        ThemeManager tm = ThemeManager.getInstance();
        tm.toggle();
        tm.applyTo(themeToggle.getScene());
        themeToggle.setText(tm.isDark() ? "☀" : "🌙");
        // Refresh inline-styled elements
        populateScore();
        populateDetails();
        populatePlans();
    }

    // ── Score card ────────────────────────────────────────────

    private void populateScore() {
        lblScore.setText(String.format("%.0f / 100", investmentScore));
        String band = InvestmentScorer.band(investmentScore);
        String hex  = InvestmentScorer.hexColor(investmentScore);

        lblRisk.setText(band);
        lblRisk.setStyle("-fx-font-size:12px;-fx-font-weight:700;-fx-text-fill:" + hex + ";");

        String reco = investmentScore >= 70
                ? "✅ Strong Investment Candidate — proceed with confidence."
                : investmentScore >= 40
                ? "⚠ Moderate Risk — additional due diligence recommended."
                : "🔴 High Risk — significant concerns identified.";
        lblRecommendation.setText(reco);
    }

    // ── Detail cards grid ─────────────────────────────────────

    private void populateDetails() {
        detailCards.getChildren().clear();
        addDetailCard("Sector",     startup.getSector());
        addDetailCard("Stage",      startup.getStage());
        addDetailCard("Status",     startup.getStatus());
        addDetailCard("Funding",
                startup.getFundingAmount() != null
                        ? String.format("$%,.2f", startup.getFundingAmount()) : "N/A");
        addDetailCard("KPI Score",
                startup.getKpiScore() != null
                        ? String.format("%.1f / 10", startup.getKpiScore()) : "N/A");
        addDetailCard("Created",
                startup.getCreationDate() != null
                        ? startup.getCreationDate().toString() : "N/A");
        addDetailCard("Incubator",  startup.getIncubatorProgram());
        addDetailCard("Description", startup.getDescription());
    }

    private void addDetailCard(String label, String value) {
        boolean dark = ThemeManager.getInstance().isDark();
        String val = (value != null && !value.isBlank()) ? value : "N/A";
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:10px;-fx-font-weight:700;-fx-text-fill:" + (dark ? "#a78bfa" : "#6d28d9") + ";");
        Label valLbl = new Label(val);
        valLbl.setWrapText(true);
        valLbl.setMaxWidth(200);
        valLbl.setStyle("-fx-font-size:12px;-fx-text-fill:" + (dark ? "rgba(220,210,255,0.90)" : "#1e1b4b") + ";-fx-font-weight:600;");

        VBox card = new VBox(4, lbl, valLbl);
        card.setPadding(new Insets(10, 14, 10, 14));
        card.setPrefWidth(180);
        card.setStyle(dark
                ? "-fx-background-color:linear-gradient(to right,#7c3aed,#a855f7), #151030;" +
                  "-fx-background-insets:0, 3 0 0 0;" +
                  "-fx-background-radius:12 12 12 12, 0 0 12 12;" +
                  "-fx-border-color:rgba(139,92,246,0.15);" +
                  "-fx-border-radius:12;-fx-border-width:0 1 1 1;"
                : "-fx-background-color:linear-gradient(to right,#7c3aed,#a855f7), white;" +
                  "-fx-background-insets:0, 3 0 0 0;" +
                  "-fx-background-radius:12 12 12 12, 0 0 12 12;" +
                  "-fx-border-color:rgba(139,92,246,0.18);" +
                  "-fx-border-radius:12;-fx-border-width:0 1 1 1;");
        detailCards.getChildren().add(card);
    }

    // ── Business plans list ───────────────────────────────────

    private void populatePlans() {
        plansBox.getChildren().clear();
        boolean dark = ThemeManager.getInstance().isDark();
        if (plans == null || plans.isEmpty()) {
            Label empty = new Label("No business plans for this startup.");
            empty.setStyle("-fx-font-size:13px;-fx-text-fill:" + (dark ? "#a78bfa" : "#6d28d9") + ";-fx-padding:12;");
            plansBox.getChildren().add(empty);
            return;
        }

        for (BusinessPlan bp : plans) {
            double planScore = InvestmentScorer.scorePlan(bp);
            String hex = InvestmentScorer.hexColor(planScore);

            Label title = new Label("📋 " + (bp.getTitle() != null ? bp.getTitle() : "Untitled"));
            title.setStyle("-fx-font-size:14px;-fx-font-weight:800;-fx-text-fill:" + (dark ? "white" : "#1e1b4b") + ";");

            Label status = new Label(bp.getStatus() != null ? bp.getStatus() : "—");
            status.setStyle("-fx-font-size:11px;-fx-font-weight:700;-fx-text-fill:" + (dark ? "#c4b5fd" : "#6d28d9") + ";" +
                    "-fx-background-color:" + (dark ? "rgba(139,92,246,0.15)" : "rgba(139,92,246,0.10)") + ";-fx-background-radius:10;-fx-padding:2 8;");

            Label score = new Label(String.format("Score: %.0f", planScore));
            score.setStyle("-fx-font-size:11px;-fx-font-weight:700;-fx-text-fill:" + hex + ";");

            Label funding = new Label(bp.getFundingRequired() != null
                    ? String.format("Funding: $%,.0f", bp.getFundingRequired()) : "Funding: N/A");
            funding.setStyle("-fx-font-size:11px;-fx-text-fill:" + (dark ? "rgba(200,190,235,0.70)" : "#374151") + ";");

            Label timeline = new Label("Timeline: " + (bp.getTimeline() != null ? bp.getTimeline() : "N/A"));
            timeline.setStyle("-fx-font-size:11px;-fx-text-fill:" + (dark ? "rgba(200,190,235,0.70)" : "#374151") + ";");

            // Export button for individual business plan
            Button btnExport = new Button("📄 Export PDF");
            btnExport.setStyle(dark
                    ? "-fx-background-color:rgba(139,92,246,0.15);-fx-text-fill:#a78bfa;" +
                      "-fx-background-radius:10;-fx-font-size:10px;-fx-font-weight:700;-fx-cursor:hand;-fx-padding:4 10;"
                    : "-fx-background-color:rgba(139,92,246,0.10);-fx-text-fill:#6d28d9;" +
                      "-fx-background-radius:10;-fx-font-size:10px;-fx-font-weight:700;-fx-cursor:hand;-fx-padding:4 10;");
            btnExport.setOnAction(e -> exportBusinessPlanPDF(bp));

            HBox topRow = new HBox(10, title, status, new Region(), score, btnExport);
            topRow.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(topRow.getChildren().get(2), Priority.ALWAYS);
            HBox infoRow = new HBox(16, funding, timeline);

            VBox planCard = new VBox(6, topRow, infoRow);
            planCard.setPadding(new Insets(12, 16, 12, 16));
            planCard.setStyle(dark
                    ? "-fx-background-color:linear-gradient(to right,#059669,#0d9488), #151030;" +
                      "-fx-background-insets:0, 3 0 0 0;" +
                      "-fx-background-radius:12 12 12 12, 0 0 12 12;" +
                      "-fx-border-color:rgba(16,185,129,0.15);" +
                      "-fx-border-radius:12;-fx-border-width:0 1 1 1;"
                    : "-fx-background-color:linear-gradient(to right,#059669,#0d9488), white;" +
                      "-fx-background-insets:0, 3 0 0 0;" +
                      "-fx-background-radius:12 12 12 12, 0 0 12 12;" +
                      "-fx-border-color:rgba(16,185,129,0.18);" +
                      "-fx-border-radius:12;-fx-border-width:0 1 1 1;");
            plansBox.getChildren().add(planCard);
        }
    }

    // ── Actions ───────────────────────────────────────────────

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/startupview.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setTitle("Startups — StartupFlow");
            Scene sc = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            ThemeManager.getInstance().applyTo(sc);
            stage.setScene(sc);
        } catch (IOException e) {
            System.err.println("[StartupDetailController.goBack] " + e.getMessage());
        }
    }

    @FXML
    private void handleExportPDF() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Startup PDF Report");
        fc.setInitialFileName("startup_" + startup.getName().replaceAll("\\W+", "_") + ".pdf");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Document (*.pdf)", "*.pdf"));
        File file = fc.showSaveDialog(mainContent.getScene().getWindow());
        if (file == null) return;

        try {
            StartupPDFExporter.exportStartupToPDF(startup, plans, investmentScore, file);
            AlertUtil.showSuccess("📄  PDF Exported",
                    "Report saved to:\n" + file.getAbsolutePath(),
                    mainContent.getScene().getWindow());
        } catch (Exception e) {
            AlertUtil.showValidationErrors(
                    List.of("PDF export failed: " + e.getMessage()),
                    mainContent.getScene().getWindow());
        }
    }

    @FXML
    private void handleGenerateQR() {
        // Start a local web server so scanning the QR opens the browser with full startup details
        StartupWebServer webServer = new StartupWebServer(startup, plans, investmentScore);
        try {
            webServer.start();
        } catch (IOException ioEx) {
            AlertUtil.showValidationErrors(
                    List.of("Could not start QR web server: " + ioEx.getMessage()),
                    mainContent.getScene().getWindow());
            return;
        }

        String qrUrl = webServer.getUrl();

        try {
            // Encode the URL — phones open the browser and see the full startup info page
            Image qrImage = QRCodeService.generateQRImageFromUrl(qrUrl, 300);

            ImageView iv = new ImageView(qrImage);
            iv.setFitWidth(280);
            iv.setFitHeight(280);
            iv.setPreserveRatio(true);

            Label lblHint = new Label("📱  Scan with any camera app — opens in browser");
            lblHint.setStyle("-fx-font-size:11px;-fx-text-fill:#a78bfa;-fx-font-weight:700;"
                    + "-fx-padding:4 12;-fx-background-color:rgba(139,92,246,0.12);"
                    + "-fx-background-radius:10;");

            Label lblUrl = new Label(qrUrl);
            lblUrl.setStyle("-fx-font-size:10px;-fx-text-fill:#a78bfa;-fx-font-family:monospace;");
            lblUrl.setWrapText(true);
            lblUrl.setMaxWidth(340);

            Button btnSave = new Button("💾  Save as PNG");
            btnSave.setStyle("-fx-background-color:linear-gradient(to right,#6d28d9,#7c3aed);"
                    + "-fx-text-fill:white;-fx-background-radius:12;-fx-padding:8 20;"
                    + "-fx-font-weight:700;-fx-font-size:12px;-fx-cursor:hand;");

            VBox content = new VBox(12, iv, lblHint, lblUrl, btnSave);
            content.setAlignment(Pos.CENTER);
            content.setPadding(new Insets(10));

            Dialog<ButtonType> dlg = new Dialog<>();
            dlg.setTitle("QR Code — " + startup.getName());
            dlg.setHeaderText("Scan this QR to view startup details on any device");
            dlg.getDialogPane().setContent(content);
            dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dlg.getDialogPane().setPrefWidth(380);
            DialogStyler.style(dlg);

            btnSave.setOnAction(e -> {
                FileChooser fc = new FileChooser();
                fc.setTitle("Save QR Code");
                fc.setInitialFileName("qr_" + startup.getName().replaceAll("\\W+", "_") + ".png");
                fc.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("PNG Image (*.png)", "*.png"));
                File file = fc.showSaveDialog(dlg.getDialogPane().getScene().getWindow());
                if (file != null) {
                    try {
                        QRCodeService.saveQRToPNGFromUrl(qrUrl, 400, file);
                        AlertUtil.showSuccess("💾  QR Saved",
                                "QR code saved to:\n" + file.getAbsolutePath(),
                                dlg.getDialogPane().getScene().getWindow());
                    } catch (Exception ex) {
                        AlertUtil.showValidationErrors(
                                List.of("Save failed: " + ex.getMessage()),
                                dlg.getDialogPane().getScene().getWindow());
                    }
                }
            });

            // Stop the server when the dialog is closed
            dlg.setOnHidden(e -> webServer.stop());
            dlg.showAndWait();

        } catch (WriterException e) {
            webServer.stop();
            AlertUtil.showValidationErrors(
                    List.of("QR generation failed: " + e.getMessage()),
                    mainContent.getScene().getWindow());
        }
    }

    private void exportBusinessPlanPDF(BusinessPlan bp) {
        double score = InvestmentScorer.scorePlan(bp);
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Business Plan PDF");
        fc.setInitialFileName("plan_" + (bp.getTitle() != null
                ? bp.getTitle().replaceAll("\\W+", "_") : bp.getBusinessPlanID()) + ".pdf");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Document (*.pdf)", "*.pdf"));
        File file = fc.showSaveDialog(mainContent.getScene().getWindow());
        if (file == null) return;

        try {
            StartupPDFExporter.exportBusinessPlanToPDF(bp, score, file);
            AlertUtil.showSuccess("📄  PDF Exported",
                    "Plan report saved to:\n" + file.getAbsolutePath(),
                    mainContent.getScene().getWindow());
        } catch (Exception e) {
            AlertUtil.showValidationErrors(
                    List.of("PDF export failed: " + e.getMessage()),
                    mainContent.getScene().getWindow());
        }
    }
}

