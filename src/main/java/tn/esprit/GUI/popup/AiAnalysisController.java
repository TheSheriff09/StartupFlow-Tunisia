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
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tn.esprit.entities.BusinessPlan;
import tn.esprit.Services.AIAnalysisResult;
import tn.esprit.Services.AIAnalyzerService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the AI Business Plan Analyzer popup (aianalysis.fxml).
 *
 * Uses {@link AIAnalyzerService} to call the OpenAI API on a background
 * {@link Task} and renders a structured four-section result:
 *   Strengths · Weaknesses · Risk Level · Improvement Suggestions
 *
 * Open via: {@link #show(StackPane, Region, BusinessPlan)}
 */
public class AiAnalysisController implements Initializable {

    // ── FXML nodes ────────────────────────────────────────────
    @FXML private StackPane         overlayRoot;
    @FXML private VBox              modalCard;
    @FXML private Label             lblPlanTitle;
    @FXML private VBox              loadingBox;
    @FXML private VBox              errorBox;
    @FXML private Label             lblError;
    // Result section nodes
    @FXML private ScrollPane        resultScroll;
    @FXML private HBox              riskBadge;
    @FXML private Label             lblRiskIcon;
    @FXML private Label             lblRiskLevel;
    @FXML private Label             lblStrengths;
    @FXML private Label             lblWeaknesses;
    @FXML private Label             lblSuggestions;

    // ── State ─────────────────────────────────────────────────
    private StackPane modalLayer;
    private Region    blurTarget;

    // ─────────────────────────────────────────────────────────
    // Static factory
    // ─────────────────────────────────────────────────────────

    /**
     * Load and display the AI Analysis popup, then start the background task.
     *
     * @param modalLayer the scene's overlay StackPane
     * @param blurTarget the main content region to blur
     * @param plan       the BusinessPlan to analyze
     */
    public static void show(StackPane modalLayer, Region blurTarget, BusinessPlan plan) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    AiAnalysisController.class.getResource("/aianalysis.fxml"));
            Node overlay = loader.load();
            AiAnalysisController ctrl = loader.getController();
            ctrl.modalLayer = modalLayer;
            ctrl.blurTarget = blurTarget;

            // Blur background
            blurTarget.setEffect(new GaussianBlur(10));

            modalLayer.setVisible(true);
            modalLayer.setManaged(true);
            modalLayer.getChildren().add(overlay);
            ctrl.animateOpen();
            ctrl.startAnalysis(plan);

        } catch (IOException e) {
            System.err.println("[AiAnalysisController.show] " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) { /* no-op */ }

    // ─────────────────────────────────────────────────────────
    // Background analysis task
    // ─────────────────────────────────────────────────────────

    /**
     * Runs the AI HTTP call on a daemon thread.
     * Updates the UI on the JavaFX Application Thread when done.
     */
    private void startAnalysis(BusinessPlan plan) {
        // Update subtitle
        if (plan.getTitle() != null && !plan.getTitle().isBlank()) {
            lblPlanTitle.setText("Plan: " + plan.getTitle());
        }

        // Show spinner immediately
        showState(State.LOADING);

        AIAnalyzerService service = new AIAnalyzerService();

        // Run the blocking HTTP call on a daemon thread so the UI stays responsive
        Task<AIAnalysisResult> task = new Task<>() {
            @Override
            protected AIAnalysisResult call() throws Exception {
                return service.analyzeBusinessPlan(plan);
            }
        };

        task.setOnSucceeded(e -> {
            AIAnalysisResult result = task.getValue();
            Platform.runLater(() -> populateResult(result));
        });

        task.setOnFailed(e -> {
            Throwable ex  = task.getException();
            String    msg = (ex != null) ? ex.getMessage() : "An unknown error occurred.";
            Platform.runLater(() -> {
                lblError.setText(msg);
                showState(State.ERROR);
            });
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    // ─────────────────────────────────────────────────────────
    // FXML handlers
    // ─────────────────────────────────────────────────────────

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
    // Result population
    // ─────────────────────────────────────────────────────────

    /**
     * Fills all four result section labels from the structured {@link AIAnalysisResult}
     * and transitions the UI to the RESULT state.
     */
    private void populateResult(AIAnalysisResult result) {
        // ── Risk badge ───────────────────────────────────────
        lblRiskIcon.setText(result.riskIcon());
        lblRiskLevel.setText(result.getRiskLevel());
        // Color both icon and label; also tint the badge background
        String color = result.riskHexColor();
        lblRiskLevel.setStyle("-fx-font-size:13px; -fx-font-weight:800; -fx-text-fill:" + color + ";");
        riskBadge.setStyle(
            "-fx-background-radius:20; -fx-padding:4 16;"
            + "-fx-border-radius:20; -fx-border-width:1.5;"
            + "-fx-background-color:" + color + "22;"
            + "-fx-border-color:" + color + "66;");

        // ── Sections ─────────────────────────────────────────
        lblStrengths.setText(result.strengthsText());
        lblWeaknesses.setText(result.weaknessesText());
        lblSuggestions.setText(result.suggestionsText());

        showState(State.RESULT);
    }

    // ─────────────────────────────────────────────────────────
    // UI state management
    // ─────────────────────────────────────────────────────────

    private enum State { LOADING, ERROR, RESULT }

    private void showState(State state) {
        loadingBox.setVisible(state == State.LOADING);
        loadingBox.setManaged(state == State.LOADING);
        errorBox.setVisible(state == State.ERROR);
        errorBox.setManaged(state == State.ERROR);
        resultScroll.setVisible(state == State.RESULT);
        resultScroll.setManaged(state == State.RESULT);

        if (state == State.RESULT || state == State.ERROR) {
            javafx.scene.Node target = (state == State.RESULT) ? resultScroll : errorBox;
            target.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(350), target);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        }
    }

    // ─────────────────────────────────────────────────────────
    // Animations
    // ─────────────────────────────────────────────────────────

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

