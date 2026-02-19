package tn.esprit.gui.businessplan;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tn.esprit.entities.BusinessPlan;

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
    @FXML private HBox   btnGroup;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private BusinessPlan plan;
    private Consumer<BusinessPlan> onEdit;
    private Consumer<BusinessPlan> onDelete;

    public void setData(BusinessPlan plan,
                        Consumer<BusinessPlan> onEdit,
                        Consumer<BusinessPlan> onDelete) {
        this.plan     = plan;
        this.onEdit   = onEdit;
        this.onDelete = onDelete;
        populate();
        attachHover();
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
        cardRoot.setOnMouseEntered(e -> {
            scale(1.03);
            fadeButtons(1.0);
        });
        cardRoot.setOnMouseExited(e -> {
            scale(1.00);
            fadeButtons(0.0);
        });
    }

    private void scale(double factor) {
        ScaleTransition st = new ScaleTransition(Duration.millis(180), cardRoot);
        st.setToX(factor);
        st.setToY(factor);
        st.play();
    }

    private void fadeButtons(double targetOpacity) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), btnGroup);
        ft.setToValue(targetOpacity);
        ft.play();
    }

    @FXML private void handleEdit()   { onEdit.accept(plan); }
    @FXML private void handleDelete() { onDelete.accept(plan); }
}
