package tn.esprit.GUI.popup;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import tn.esprit.Services.CurrencyService;
import tn.esprit.Services.CurrencyService.CurrencyException;
import tn.esprit.utils.FormValidator;
import tn.esprit.utils.ValidationUtil;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the Currency Exchange popup (currency.fxml).
 *
 * Three UI states:
 *   INPUT   — amount field + Convert button
 *   LOADING — spinner during HTTP request
 *   RESULT  — USD and EUR cards with formatted amounts
 *
 * Open via: {@link #show(StackPane, Region)} or
 *           {@link #show(StackPane, Region, double)} to pre-fill the amount
 *           (e.g. from a startup's fundingRequired field).
 */
public class CurrencyController implements Initializable {

    // ── FXML nodes ────────────────────────────────────────────
    @FXML private StackPane overlayRoot;
    @FXML private VBox      modalCard;

    // Input state
    @FXML private VBox      inputBox;
    @FXML private TextField tfAmount;
    @FXML private Label     errAmount;
    @FXML private Button    btnConvert;

    // Loading state
    @FXML private VBox      loadingBox;

    // Result state
    @FXML private VBox      resultBox;
    @FXML private Label     lblSourceAmount;
    @FXML private Label     lblUSD;
    @FXML private Label     lblEUR;
    @FXML private Label     lblRateUSD;
    @FXML private Label     lblRateEUR;
    @FXML private Label     lblUpdated;

    // ── State ─────────────────────────────────────────────────
    private StackPane modalLayer;
    private Region    blurTarget;

    private enum State { INPUT, LOADING, RESULT }

    // ─────────────────────────────────────────────────────────
    // Static factories
    // ─────────────────────────────────────────────────────────

    /**
     * Opens the popup with an empty amount field.
     */
    public static void show(StackPane modalLayer, Region blurTarget) {
        show(modalLayer, blurTarget, -1);
    }

    /**
     * Opens the popup and pre-fills the amount field with {@code prefillAmount}
     * (pass ≤ 0 to leave the field empty).
     */
    public static void show(StackPane modalLayer, Region blurTarget, double prefillAmount) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    CurrencyController.class.getResource("/currency.fxml"));
            Node overlay = loader.load();
            CurrencyController ctrl = loader.getController();
            ctrl.modalLayer = modalLayer;
            ctrl.blurTarget = blurTarget;

            // Pre-fill amount if provided (e.g. from startup funding field)
            if (prefillAmount > 0) {
                ctrl.tfAmount.setText(String.format("%.2f", prefillAmount));
            }

            blurTarget.setEffect(new GaussianBlur(10));
            modalLayer.setVisible(true);
            modalLayer.setManaged(true);
            modalLayer.getChildren().add(overlay);
            ctrl.animateOpen();

        } catch (IOException e) {
            System.err.println("[CurrencyController.show] " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    // Initializable
    // ─────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tfAmount.setTextFormatter(ValidationUtil.unsignedDecimalFormatter());

        // Real-time validation — must be > 0
        FormValidator.validateDoubleOnType(tfAmount, errAmount, true);

        // Disable Convert until field is non-empty
        FormValidator.bindSubmitButton(btnConvert, tfAmount);
    }

    // ─────────────────────────────────────────────────────────
    // FXML handlers
    // ─────────────────────────────────────────────────────────

    @FXML
    private void handleConvert() {
        // ── Final validation ──────────────────────────────────
        if (!FormValidator.requirePositiveDouble(tfAmount, errAmount, true)) return;

        double amount = Double.parseDouble(tfAmount.getText().trim());

        showState(State.LOADING);
        startConversionTask(amount);
    }

    @FXML
    private void handleReset() {
        tfAmount.clear();
        errAmount.setText("");
        showState(State.INPUT);
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

    @FXML
    private void handleOverlayClick(javafx.scene.input.MouseEvent e) {
        if (e.getTarget() == overlayRoot) handleClose();
    }

    // ─────────────────────────────────────────────────────────
    // Background task
    // ─────────────────────────────────────────────────────────

    /**
     * Runs the CurrencyService HTTP call on a daemon thread so the UI stays
     * responsive. On success, populates the result cards. On failure, shows
     * the error inline without crashing.
     */
    private void startConversionTask(double amount) {
        CurrencyService service = new CurrencyService();

        Task<Map<String, Double>> task = new Task<>() {
            @Override
            protected Map<String, Double> call() throws CurrencyException {
                // One HTTP call fetches both USD and EUR rates simultaneously
                return service.convertToMultiple("TND", amount, "USD", "EUR");
            }
        };

        task.setOnSucceeded(e -> {
            Map<String, Double> results = task.getValue();
            Platform.runLater(() -> populateResult(amount, results));
        });

        task.setOnFailed(e -> {
            Throwable ex  = task.getException();
            String    msg = (ex != null) ? ex.getMessage() : "Unknown error occurred.";
            Platform.runLater(() -> {
                // Return to INPUT state and show the error under the field
                showState(State.INPUT);
                errAmount.setText("⚠ " + msg);
            });
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    // ─────────────────────────────────────────────────────────
    // Result population
    // ─────────────────────────────────────────────────────────

    private void populateResult(double amount, Map<String, Double> results) {
        lblSourceAmount.setText(String.format("%,.2f TND", amount));

        double usd = results.getOrDefault("USD", 0.0);
        double eur = results.getOrDefault("EUR", 0.0);

        lblUSD.setText(String.format("$ %,.2f", usd));
        lblEUR.setText(String.format("€ %,.2f", eur));

        // Show implied exchange rates
        if (amount > 0) {
            lblRateUSD.setText(String.format("Rate: 1 TND = %.4f USD", usd / amount));
            lblRateEUR.setText(String.format("Rate: 1 TND = %.4f EUR", eur / amount));
        }

        lblUpdated.setText("Rates sourced from open.er-api.com  ·  Live data");

        showState(State.RESULT);

        // Fade the result box in
        resultBox.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(320), resultBox);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    // ─────────────────────────────────────────────────────────
    // State management
    // ─────────────────────────────────────────────────────────

    private void showState(State state) {
        inputBox.setVisible(state == State.INPUT);
        inputBox.setManaged(state == State.INPUT);

        loadingBox.setVisible(state == State.LOADING);
        loadingBox.setManaged(state == State.LOADING);

        resultBox.setVisible(state == State.RESULT);
        resultBox.setManaged(state == State.RESULT);
    }

    // ─────────────────────────────────────────────────────────
    // Animations
    // ─────────────────────────────────────────────────────────

    private void animateOpen() {
        overlayRoot.setOpacity(0);
        modalCard.setScaleX(0.82);
        modalCard.setScaleY(0.82);

        FadeTransition  ft = new FadeTransition(Duration.millis(250), overlayRoot);
        ft.setFromValue(0); ft.setToValue(1);

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

