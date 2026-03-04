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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.GaussianBlur;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;
import tn.esprit.Services.ForecastRow;
import tn.esprit.Services.GeminiForecastService;
import tn.esprit.utils.ForecastExportUtil;
import tn.esprit.utils.FormValidator;
import tn.esprit.utils.ValidationUtil;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the AI Financial Forecast popup (financialforecast.fxml).
 *
 * Three UI states:
 *   INPUT   — user fills in revenue, growth rate, expenses
 *   LOADING — spinner shown while Gemini API call runs on a daemon thread
 *   RESULT  — scrollable forecast table + investment recommendation
 *
 * Open via: {@link #show(StackPane, Region)}
 */
public class FinancialForecastController implements Initializable {

    // ── FXML nodes ────────────────────────────────────────────
    @FXML private StackPane  overlayRoot;
    @FXML private VBox       modalCard;

    // Input state
    @FXML private VBox       inputBox;
    @FXML private TextField  tfRevenue;
    @FXML private TextField  tfGrowthRate;
    @FXML private TextField  tfExpenses;
    @FXML private Label      errRevenue;
    @FXML private Label      errGrowthRate;
    @FXML private Label      errExpenses;
    @FXML private Button     btnGenerate;

    // Loading state
    @FXML private VBox       loadingBox;

    // Result state
    @FXML private VBox       resultBox;
    @FXML private HBox       badgeRow;
    @FXML private ScrollPane resultScroll;
    @FXML private Label      lblForecast;
    @FXML private Button     btnExport;

    // ── State ─────────────────────────────────────────────────────────
    private StackPane          modalLayer;
    private Region             blurTarget;
    private List<ForecastRow>  forecastRows;
    private String             startupName = "";

    // Three possible panel states
    private enum State { INPUT, LOADING, RESULT }

    // ─────────────────────────────────────────────────────────
    // Static factory
    // ─────────────────────────────────────────────────────────

    /**
     * Loads and displays the Financial Forecast popup.
     *
     * @param modalLayer the scene's overlay StackPane
     * @param blurTarget the main content region to blur
     */
    public static void show(StackPane modalLayer, Region blurTarget) {
        show(modalLayer, blurTarget, "");
    }

    /**
     * Opens the popup and stores the startup name for use in exported reports.
     *
     * @param startupName startup name shown in PDF/Excel headers (may be empty)
     */
    public static void show(StackPane modalLayer, Region blurTarget, String startupName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    FinancialForecastController.class.getResource("/financialforecast.fxml"));
            Node overlay = loader.load();
            FinancialForecastController ctrl = loader.getController();
            ctrl.modalLayer  = modalLayer;
            ctrl.blurTarget  = blurTarget;
            ctrl.startupName = (startupName != null) ? startupName : "";

            // Blur the background content
            blurTarget.setEffect(new GaussianBlur(10));

            modalLayer.setVisible(true);
            modalLayer.setManaged(true);
            modalLayer.getChildren().add(overlay);
            ctrl.animateOpen();

        } catch (IOException e) {
            System.err.println("[FinancialForecastController.show] " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    // Initializable
    // ─────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tfRevenue.setTextFormatter(ValidationUtil.unsignedDecimalFormatter());
        tfGrowthRate.setTextFormatter(ValidationUtil.unsignedDecimalFormatter());
        tfExpenses.setTextFormatter(ValidationUtil.unsignedDecimalFormatter());

        // Real-time semantic validation
        FormValidator.validateDoubleOnType(tfRevenue,    errRevenue,    true);
        FormValidator.validateDoubleOnType(tfGrowthRate, errGrowthRate, true);
        FormValidator.validateDoubleOnType(tfExpenses,   errExpenses,   true);

        // Disable Generate button until all three fields are non-empty
        FormValidator.bindSubmitButton(btnGenerate, tfRevenue, tfGrowthRate, tfExpenses);
    }

    // ─────────────────────────────────────────────────────────
    // FXML handlers
    // ─────────────────────────────────────────────────────────

    /** Triggered when the user clicks "Generate Forecast". */
    @FXML
    private void handleGenerate() {
        // ── Final validation before sending ──────────────────
        boolean revenueOk  = FormValidator.requireDouble(tfRevenue,  errRevenue,  true);
        boolean expensesOk = FormValidator.requireDouble(tfExpenses, errExpenses, true);

        Optional<String> growthError = ValidationUtil.checkGrowthRate(tfGrowthRate.getText());
        boolean growthOk = growthError.isEmpty();
        if (growthError.isPresent()) {
            errGrowthRate.setText(growthError.get());
            errGrowthRate.setVisible(true);
            errGrowthRate.setManaged(true);
        } else {
            errGrowthRate.setText("");
            errGrowthRate.setVisible(false);
            errGrowthRate.setManaged(false);
        }

        if (!revenueOk || !growthOk || !expensesOk) return;

        // ── Parse values ──────────────────────────────────────
        double revenue    = Double.parseDouble(tfRevenue.getText().trim());
        double growthRate = Double.parseDouble(tfGrowthRate.getText().trim());
        double expenses   = Double.parseDouble(tfExpenses.getText().trim());

        // ── Show spinner and start background task ────────────
        showState(State.LOADING);
        startForecastTask(revenue, growthRate, expenses);
    }

    /** Resets to INPUT state so the user can enter new values. */
    @FXML
    private void handleNewForecast() {
        tfRevenue.clear();
        tfGrowthRate.clear();
        tfExpenses.clear();
        errRevenue.setText("");
        errGrowthRate.setText("");
        errExpenses.setText("");
        badgeRow.getChildren().clear();
        lblForecast.setText("");
        forecastRows = null; // clear export data
        showState(State.INPUT);
    }

    /** Closes the popup with a fade-out animation. */
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

    /** Closes popup when the dark backdrop (not the card) is clicked. */
    @FXML
    private void handleOverlayClick(javafx.scene.input.MouseEvent e) {
        if (e.getTarget() == overlayRoot) handleClose();
    }

    /**
     * Shows a ChoiceDialog asking for PDF or Excel, then delegates to
     * {@link ForecastExportUtil} which handles the FileChooser and file generation.
     */
    @FXML
    private void handleExport() {
        // Guard: should not be reachable if button is disabled, but be safe
        if (forecastRows == null || forecastRows.isEmpty()) {
            tn.esprit.utils.AlertUtil.showWarning("No forecast data to export.",
                    overlayRoot.getScene().getWindow());
            return;
        }

        // ── Build a styled export-format picker dialog ────────────────
        ToggleGroup group = new ToggleGroup();

        RadioButton rbPdf   = new RadioButton("\uD83D\uDCC4  Export as PDF");
        RadioButton rbExcel = new RadioButton("\uD83D\uDCCA  Export as Excel (.xlsx)");
        rbPdf.setToggleGroup(group);
        rbExcel.setToggleGroup(group);
        rbPdf.setSelected(true);

        String rbStyle =
            "-fx-text-fill: rgba(220,210,255,0.95);" +
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;";
        rbPdf.setStyle(rbStyle);
        rbExcel.setStyle(rbStyle);

        String cardStyle =
            "-fx-background-color: rgba(255,255,255,0.07);" +
            "-fx-border-color: rgba(139,92,246,0.35);" +
            "-fx-border-radius: 12; -fx-background-radius: 12;" +
            "-fx-padding: 12 16 12 16; -fx-cursor: hand;";

        Label subPdf   = optSubLabel("Portable Document Format \u2014 best for sharing");
        Label subExcel = optSubLabel("Excel workbook \u2014 best for further analysis");

        VBox cardPdf   = new VBox(5, rbPdf, subPdf);   cardPdf.setStyle(cardStyle);
        VBox cardExcel = new VBox(5, rbExcel, subExcel); cardExcel.setStyle(cardStyle);
        cardPdf.setOnMouseClicked(e   -> rbPdf.setSelected(true));
        cardExcel.setOnMouseClicked(e -> rbExcel.setSelected(true));

        VBox optionsBox = new VBox(12, cardPdf, cardExcel);
        optionsBox.setPadding(new Insets(18, 28, 20, 28));
        optionsBox.setStyle("-fx-background-color: transparent;");

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Export Financial Forecast");
        dlg.setHeaderText("Choose an export format");
        dlg.getDialogPane().setContent(optionsBox);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.getDialogPane().setPrefWidth(420);
        dlg.initOwner(overlayRoot.getScene().getWindow());
        DialogStyler.style(dlg);

        Optional<ButtonType> result = dlg.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        Window owner = overlayRoot.getScene().getWindow();
        if (rbPdf.isSelected()) {
            ForecastExportUtil.exportToPDF(forecastRows, startupName, owner);
        } else {
            ForecastExportUtil.exportToExcel(forecastRows, startupName, owner);
        }
    }

    /** Small subtitle label for export option cards. */
    private static Label optSubLabel(String text) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-text-fill: rgba(180,160,240,0.78);" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 0 0 0 4;");
        return l;
    }

    // ─────────────────────────────────────────────────────────
    // Background task
    // ─────────────────────────────────────────────────────────

    /**
     * Runs GeminiForecastService.generateForecast() on a daemon thread.
     * Updates UI on the JavaFX Application Thread when done.
     */
    private void startForecastTask(double revenue, double growthRate, double expenses) {
        GeminiForecastService service = new GeminiForecastService();

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                // This blocking HTTP call runs off the JavaFX thread
                return service.generateForecast(revenue, growthRate, expenses);
            }
        };

        task.setOnSucceeded(e -> {
            String forecast = task.getValue();
            Platform.runLater(() -> populateResult(forecast, revenue, growthRate, expenses));
        });

        task.setOnFailed(e -> {
            // GeminiForecastService never throws — it always returns a string.
            // This branch is a safety net for unexpected runtime errors.
            Throwable ex = task.getException();
            String fallback = "[Error generating forecast]\n" +
                              (ex != null ? ex.getMessage() : "Unknown error.");
            Platform.runLater(() -> populateResult(fallback, revenue, growthRate, expenses));
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    // ─────────────────────────────────────────────────────────
    // Result population
    // ─────────────────────────────────────────────────────────

    /**
     * Fills the result panel with the forecast text and summary badges,
     * then transitions to the RESULT state.
     */
    private void populateResult(String forecast, double revenue, double growthRate, double expenses) {        // Compute structured rows for export (always local, no API call)
        forecastRows = GeminiForecastService.computeForecastRows(revenue, growthRate, expenses);
        // ── Summary input badges ──────────────────────────────
        badgeRow.getChildren().clear();
        badgeRow.getChildren().addAll(
            makeBadge("💰 Revenue",  String.format("%,.0f TND", revenue),  "#059669", "#d1fae5"),
            makeBadge("📈 Growth",   String.format("%.1f%%",    growthRate), "#0d9488", "#ccfbf1"),
            makeBadge("💸 Expenses", String.format("%,.0f TND", expenses),  "#7c3aed", "#ede9fe")
        );

        // ── Forecast text ─────────────────────────────────────
        lblForecast.setText(forecast);

        // ── Show result with fade-in ──────────────────────────
        showState(State.RESULT);
        resultBox.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(300), resultBox);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /** Creates a small colored summary badge label. */
    private Label makeBadge(String prefix, String value, String borderColor, String bgColor) {
        Label badge = new Label(prefix + ":  " + value);
        badge.setStyle(
            "-fx-background-color:" + bgColor + ";" +
            "-fx-border-color:" + borderColor + ";" +
            "-fx-border-radius:20; -fx-background-radius:20;" +
            "-fx-padding:3 12; -fx-font-size:11px; -fx-font-weight:700;" +
            "-fx-text-fill:" + borderColor + ";");
        return badge;
    }

    // ─────────────────────────────────────────────────────────
    // State management
    // ─────────────────────────────────────────────────────────

    /** Shows the given state panel and hides the other two. */
    private void showState(State state) {
        inputBox.setVisible(state == State.INPUT);
        inputBox.setManaged(state == State.INPUT);

        loadingBox.setVisible(state == State.LOADING);
        loadingBox.setManaged(state == State.LOADING);

        resultBox.setVisible(state == State.RESULT);
        resultBox.setManaged(state == State.RESULT);
    }

    // ─────────────────────────────────────────────────────────
    // Open / close animations
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

