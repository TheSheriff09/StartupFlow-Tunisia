package tn.esprit.GUI.popup;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tn.esprit.Services.FundingSimulator;
import tn.esprit.Services.SimulationResult;
import tn.esprit.utils.FormValidator;
import tn.esprit.utils.ValidationUtil;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Funding Simulation popup (fundingsimulation.fxml).
 *
 * Open via: {@link #show(StackPane, Region)}
 *
 * The popup is injected into the scene's modalLayer StackPane,
 * so the background stays visible with a Gaussian blur.
 */
public class FundingSimulationController implements Initializable {

    // ── FXML nodes ────────────────────────────────────────────
    @FXML private StackPane overlayRoot;
    @FXML private VBox      modalCard;
    @FXML private TextField tfFunding;
    @FXML private TextField tfExpenses;
    @FXML private TextField tfRevenue;
    @FXML private Label     errFunding;
    @FXML private Label     errExpenses;
    @FXML private Label     errRevenue;
    @FXML private VBox      resultBox;
    @FXML private Label     lblStatusIcon;
    @FXML private Label     lblStatus;
    @FXML private Label     lblBurnRate;
    @FXML private Label     lblRunway;
    @FXML private Label     lblSummary;
    @FXML private Button    btnSimulate;

    // ── State ─────────────────────────────────────────────────
    private StackPane modalLayer;
    private Region    blurTarget;
    private final FundingSimulator simulator = new FundingSimulator();

    // ─────────────────────────────────────────────────────────
    // Static factory
    // ─────────────────────────────────────────────────────────

    /**
     * Load and display the Funding Simulation popup.
     *
     * @param modalLayer the scene's overlay StackPane
     * @param blurTarget the main content region to blur
     */
    public static void show(StackPane modalLayer, Region blurTarget) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    FundingSimulationController.class.getResource("/fundingsimulation.fxml"));
            Node overlay = loader.load();
            FundingSimulationController ctrl = loader.getController();
            ctrl.modalLayer  = modalLayer;
            ctrl.blurTarget  = blurTarget;

            // Blur the background content
            blurTarget.setEffect(new GaussianBlur(10));

            modalLayer.setVisible(true);
            modalLayer.setManaged(true);
            modalLayer.getChildren().add(overlay);
            ctrl.animateOpen();
        } catch (IOException e) {
            System.err.println("[FundingSimulationController.show] " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    // Initializable
    // ─────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tfFunding.setTextFormatter(ValidationUtil.unsignedDecimalFormatter());
        tfExpenses.setTextFormatter(ValidationUtil.unsignedDecimalFormatter());
        tfRevenue.setTextFormatter(ValidationUtil.unsignedDecimalFormatter());

        // Real-time semantic validation (negative values, zero funding, empty required field)
        FormValidator.validateDoubleOnType(tfFunding,  errFunding,  true);
        FormValidator.validateDoubleOnType(tfExpenses, errExpenses, false);
        FormValidator.validateDoubleOnType(tfRevenue,  errRevenue,  false);

        // Disable the Simulate button until all three fields contain text
        FormValidator.bindSubmitButton(btnSimulate, tfFunding, tfExpenses, tfRevenue);
    }

    // ─────────────────────────────────────────────────────────
    // FXML handlers
    // ─────────────────────────────────────────────────────────

    @FXML
    private void handleSimulate() {
        resultBox.setVisible(false);
        resultBox.setManaged(false);

        // ── Validate all fields ──────────────────────────────
        // Funding: required AND must be > 0 (zero funding is nonsensical)
        boolean fundingOk  = FormValidator.requireNonEmpty(tfFunding,  errFunding)
                          && FormValidator.requirePositiveDouble(tfFunding,  errFunding,  true);
        // Expenses and revenue: required, must be ≥ 0
        boolean expensesOk = FormValidator.requireNonEmpty(tfExpenses, errExpenses)
                          && FormValidator.requireDouble(tfExpenses, errExpenses, false);
        boolean revenueOk  = FormValidator.requireNonEmpty(tfRevenue,  errRevenue)
                          && FormValidator.requireDouble(tfRevenue,  errRevenue,  false);

        if (!fundingOk || !expensesOk || !revenueOk) return;

        // ── Parse values ─────────────────────────────────────
        double funding  = Double.parseDouble(tfFunding.getText().trim());
        double expenses = Double.parseDouble(tfExpenses.getText().trim());
        double revenue  = Double.parseDouble(tfRevenue.getText().trim());

        // ── Run simulation ───────────────────────────────────
        SimulationResult result;
        try {
            result = simulator.simulate(funding, expenses, revenue);
        } catch (IllegalArgumentException ex) {
            PopupManager.showError(modalLayer, blurTarget, ex.getMessage());
            return;
        }

        // ── Populate result UI ───────────────────────────────
        showResult(result);
    }

    @FXML
    private void handleClose() {
        animateClose(() -> {
            if (blurTarget != null) blurTarget.setEffect(null);
            if (modalLayer != null) {
                modalLayer.getChildren().removeIf(n -> n == overlayRoot);
                if (modalLayer.getChildren().isEmpty()) {
                    modalLayer.setVisible(false);
                    modalLayer.setManaged(false);
                }
            }
        });
    }

    /** Close when user clicks the dark overlay backdrop (not the card). */
    @FXML
    private void handleOverlayClick(javafx.scene.input.MouseEvent e) {
        if (e.getTarget() == overlayRoot) handleClose();
    }

    // ─────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────

    private void showResult(SimulationResult r) {
        // Status icon + label with color
        lblStatusIcon.setText(r.statusIcon());
        lblStatus.setText(r.getStatus().name());
        lblStatus.setStyle("-fx-font-size:15px; -fx-font-weight:800; -fx-text-fill:"
                + r.statusHexColor() + ";");

        // Burn rate
        if (r.getBurnRate() <= 0) {
            lblBurnRate.setText("🔥 Burn Rate: $0 /mo  (revenue ≥ expenses)");
        } else {
            lblBurnRate.setText(String.format("🔥 Burn Rate: $%,.0f / month", r.getBurnRate()));
        }

        // Runway
        if (r.isInfiniteRunway()) {
            lblRunway.setText("🛫 Runway: Infinite  (cash-flow positive)");
        } else {
            lblRunway.setText(String.format("🛫 Runway: %.1f months  (≈ %.0f years)",
                    r.getRunwayMonths(), r.getRunwayMonths() / 12.0));
        }

        // Summary
        lblSummary.setText(r.getSummary());

        // Show the result card with a fade-in
        resultBox.setVisible(true);
        resultBox.setManaged(true);
        resultBox.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(300), resultBox);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void animateOpen() {
        overlayRoot.setOpacity(0);
        modalCard.setScaleX(0.82);
        modalCard.setScaleY(0.82);

        FadeTransition ft = new FadeTransition(Duration.millis(250), overlayRoot);
        ft.setFromValue(0);
        ft.setToValue(1);

        ScaleTransition st = new ScaleTransition(Duration.millis(320), modalCard);
        st.setFromX(0.82); st.setToX(1.0);
        st.setFromY(0.82); st.setToY(1.0);
        st.setInterpolator(Interpolator.SPLINE(0.22, 0.61, 0.36, 1.0));

        new ParallelTransition(ft, st).play();
    }

    private void animateClose(Runnable onFinished) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), overlayRoot);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> onFinished.run());
        ft.play();
    }
}

