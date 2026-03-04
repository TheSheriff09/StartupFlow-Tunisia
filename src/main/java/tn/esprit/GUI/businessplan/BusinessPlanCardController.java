package tn.esprit.GUI.businessplan;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tn.esprit.entities.BusinessPlan;
import tn.esprit.GUI.popup.AiAnalysisController;
import tn.esprit.Services.InvestmentScorer;
import tn.esprit.Services.StartupPDFExporter;
import tn.esprit.utils.AlertUtil;

import java.io.File;
import java.util.function.Consumer;

public class BusinessPlanCardController {

    @FXML private VBox   cardRoot;
    @FXML private Label  lblStatus;
    @FXML private Label  lblTitle;
    @FXML private Label  lblDescription;
    @FXML private Label  lblMarketAnalysis;
    @FXML private Label  lblFunding;
    @FXML private Label  lblTimeline;
    @FXML private Label  lblDates;
    @FXML private Button btnMore;

    private BusinessPlan plan;
    private Consumer<BusinessPlan> onEdit;
    private Consumer<BusinessPlan> onDelete;

    // ── modalLayer / blurTarget injected by the parent view ───
    private StackPane modalLayer;
    private Region    blurTarget;

    // ── Context menu ──────────────────────────────────────────
    private ContextMenu contextMenu;

    /**
     * Full injection including the modal overlay refs for AI popup.
     */
    public void setData(BusinessPlan plan,
                        Consumer<BusinessPlan> onEdit,
                        Consumer<BusinessPlan> onDelete,
                        StackPane modalLayer,
                        Region blurTarget) {
        this.plan        = plan;
        this.onEdit      = onEdit;
        this.onDelete    = onDelete;
        this.modalLayer  = modalLayer;
        this.blurTarget  = blurTarget;
        buildContextMenu();
        populate();
        attachHover();
    }

    /**
     * Backward-compatible overload (no modal refs — AI menu item will be hidden).
     */
    public void setData(BusinessPlan plan,
                        Consumer<BusinessPlan> onEdit,
                        Consumer<BusinessPlan> onDelete) {
        setData(plan, onEdit, onDelete, null, null);
    }

    // ── Context menu ──────────────────────────────────────────

    private void buildContextMenu() {
        contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("card-context-menu");

        MenuItem editItem = new MenuItem("✏  Edit");
        editItem.getStyleClass().add("menu-item-card");
        editItem.setOnAction(e -> onEdit.accept(plan));

        MenuItem analyzeItem = new MenuItem("🤖  AI Analysis");
        analyzeItem.getStyleClass().add("menu-item-card");
        analyzeItem.setOnAction(e -> handleAnalyze());
        // Disable when modal refs not provided
        if (modalLayer == null || blurTarget == null) {
            analyzeItem.setDisable(true);
        }

        MenuItem exportItem = new MenuItem("📄  Export PDF");
        exportItem.getStyleClass().add("menu-item-card");
        exportItem.setOnAction(e -> handleExport());

        MenuItem deleteItem = new MenuItem("🗑  Delete");
        deleteItem.getStyleClass().add("menu-item-delete");
        deleteItem.setOnAction(e -> onDelete.accept(plan));

        contextMenu.getItems().addAll(
                editItem, analyzeItem, exportItem,
                new SeparatorMenuItem(), deleteItem);
    }

    private void populate() {
        // Status badge
        String status = plan.getStatus() != null ? plan.getStatus() : "Draft";
        lblStatus.setText(status.toUpperCase());
        lblStatus.getStyleClass().removeAll(
            "status-draft", "status-active", "status-archived",
            "status-underreview", "status-pending", "status-funded");
        lblStatus.getStyleClass().add("status-" + status.toLowerCase().replace(" ", ""));

        lblTitle.setText(plan.getTitle() != null ? plan.getTitle() : "(No title)");

        String desc = plan.getDescription() != null ? plan.getDescription() : "";
        lblDescription.setText(desc.length() > 90 ? desc.substring(0, 90) + "…" : desc);

        String ma = plan.getMarketAnalysis() != null ? plan.getMarketAnalysis() : "";
        lblMarketAnalysis.setText(ma.isEmpty() ? "" : "📊 " + (ma.length() > 75 ? ma.substring(0, 75) + "…" : ma));

        lblFunding.setText(plan.getFundingRequired() != null
                ? "💰 " + String.format("%,.0f TND", plan.getFundingRequired()) : "");
        lblTimeline.setText(plan.getTimeline() != null ? "⏱ " + plan.getTimeline() : "");

        String created = plan.getCreationDate() != null ? "Created: " + plan.getCreationDate() : "";
        String updated = plan.getLastUpdate()   != null ? " · Updated: " + plan.getLastUpdate() : "";
        lblDates.setText(created + updated);
    }

    private void attachHover() {
        cardRoot.setOnMouseEntered(e -> scale(1.03));
        cardRoot.setOnMouseExited(e  -> scale(1.00));
    }

    private void scale(double factor) {
        ScaleTransition st = new ScaleTransition(Duration.millis(180), cardRoot);
        st.setToX(factor);
        st.setToY(factor);
        st.play();
    }

    // ── "More" button handler ─────────────────────────────────

    @FXML
    private void handleMore() {
        contextMenu.show(btnMore, javafx.geometry.Side.BOTTOM, 0, 4);
    }

    /** Exports this business plan to a PDF file. */
    private void handleExport() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Export Business Plan PDF");
        String safeName = plan.getTitle() != null ? plan.getTitle().replaceAll("\\s+", "_") : "plan";
        fc.setInitialFileName(safeName + "_Report.pdf");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = fc.showSaveDialog(cardRoot.getScene().getWindow());
        if (file == null) return;
        try {
            double score = InvestmentScorer.scorePlan(plan);
            StartupPDFExporter.exportBusinessPlanToPDF(plan, score, file);
            AlertUtil.showSuccess("PDF exported to " + file.getName(),
                    cardRoot.getScene().getWindow());
        } catch (Exception e) {
            AlertUtil.showValidationErrors(
                    java.util.List.of("PDF export failed: " + e.getMessage()),
                    cardRoot.getScene().getWindow());
        }
    }

    /** Opens the AI analysis popup for this business plan — asks for confirmation first. */
    private void handleAnalyze() {
        if (modalLayer == null || blurTarget == null) return;

        boolean confirmed = AlertUtil.confirm(
            "AI Risk Analysis",
            "Run AI Risk Analysis for \"" + plan.getTitle() + "\"?",
            "This will call the Gemini AI to evaluate the business plan.\n" +
            "The analysis takes a few seconds. Proceed?",
            modalLayer.getScene() != null ? modalLayer.getScene().getWindow() : null);

        if (!confirmed) return;
        AiAnalysisController.show(modalLayer, blurTarget, plan);
    }
}

