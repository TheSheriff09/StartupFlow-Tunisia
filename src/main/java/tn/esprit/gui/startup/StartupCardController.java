package tn.esprit.gui.startup;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tn.esprit.entities.Startup;

import java.util.function.Consumer;

/**
 * Controller for a single startup card (startupcard.fxml).
 * The parent StartupViewController injects the Startup data
 * and three callback lambdas after loading this FXML.
 */
public class StartupCardController {

    // ── FXML nodes ────────────────────────────────────────────
    @FXML private VBox   cardRoot;
    @FXML private HBox   btnGroup;
    @FXML private Label  lblSector;
    @FXML private Label  lblName;
    @FXML private Label  lblDescription;
    @FXML private Label  lblDate;
    @FXML private Label  lblStatus;
    @FXML private Label  lblFunding;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnViewPlans;

    // ── State ─────────────────────────────────────────────────
    private Startup startup;
    private Consumer<Startup> onEdit;
    private Consumer<Startup> onDelete;
    private Consumer<Startup> onViewPlans;

    // ── Injection from parent ─────────────────────────────────

    /**
     * Called by StartupViewController after FXMLLoader.load()
     *
     * @param startup      the startup to display
     * @param onEdit       callback when user clicks Edit
     * @param onDelete     callback when user clicks Delete
     * @param onViewPlans  callback when user clicks Plans (or the card)
     */
    public void setData(Startup startup,
                        Consumer<Startup> onEdit,
                        Consumer<Startup> onDelete,
                        Consumer<Startup> onViewPlans) {
        this.startup     = startup;
        this.onEdit      = onEdit;
        this.onDelete    = onDelete;
        this.onViewPlans = onViewPlans;
        populate();
        attachHover();
    }

    // ── Populate labels ───────────────────────────────────────

    private void populate() {
        lblName.setText(startup.getName() != null ? startup.getName() : "—");

        String sector = startup.getSector() != null ? startup.getSector() : "General";
        lblSector.setText("• Sector: " + sector);

        String stage = startup.getStage() != null ? startup.getStage() : "Seed";
        lblDescription.setText("• Stage: " + stage);

        String mentor = (startup.getIncubatorProgram() != null && !startup.getIncubatorProgram().isBlank())
                ? startup.getIncubatorProgram() : "Not assigned";
        lblStatus.setText("• Mentor: " + mentor);

        String status = startup.getStatus() != null ? startup.getStatus() : "—";
        lblFunding.setText("• Status: " + status);

        lblDate.setText("");
    }

    // ── Hover animation ───────────────────────────────────────

    private void attachHover() {
        cardRoot.setOnMouseEntered(e -> {
            scale(1.03);
            fadeButtons(1.0);
        });
        cardRoot.setOnMouseExited(e -> {
            scale(1.00);
            fadeButtons(0.0);
        });

        // Clicking card body (not buttons) → view plans
        cardRoot.setOnMouseClicked(e -> {
            if (e.getTarget() != btnEdit
                    && e.getTarget() != btnDelete
                    && e.getTarget() != btnViewPlans) {
                onViewPlans.accept(startup);
            }
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

    // ── Button handlers (wired in FXML) ───────────────────────

    @FXML
    private void handleEdit() {
        onEdit.accept(startup);
    }

    @FXML
    private void handleDelete() {
        onDelete.accept(startup);
    }

    @FXML
    private void handleViewPlans() {
        onViewPlans.accept(startup);
    }
}
