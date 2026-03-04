package tn.esprit.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.stage.Window;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import tn.esprit.utils.DesignTokens;
import tn.esprit.utils.ThemeManager;

/**
 * AlertUtil — Centralized, styled alert factory for StartupFlow.
 *
 * All public methods produce a purple-themed, consistently laid-out dialog
 * that matches the application's design system.  Every method accepts a
 * nullable {@code owner} window; pass {@code null} when you don't have a
 * scene reference handy (the dialog will still appear center-screen).
 *
 * Common usage:
 * <pre>
 *   // Simple success notification
 *   AlertUtil.showSuccess("Startup created successfully!", owner);
 *
 *   // Blocking confirmation — returns true when user clicks OK
 *   if (!AlertUtil.confirm("Delete startup?", "This action cannot be undone.", owner)) return;
 *
 *   // Show all validation errors from ValidationUtil
 *   AlertUtil.showValidationErrors(errors, owner);
 * </pre>
 */
public final class AlertUtil {

    // ── All style tokens now sourced from DesignTokens.java ───

    // Stop instantiation
    private AlertUtil() {}

    // ─────────────────────────────────────────────────────────
    // Informational / Feedback alerts
    // ─────────────────────────────────────────────────────────

    /**
     * Shows a professional success alert with a green checkmark header.
     *
     * @param message the body text
     * @param owner   parent window (nullable)
     */
    public static void showSuccess(String message, Window owner) {
        Alert alert = build(AlertType.INFORMATION, "✅  Success", null, message, owner);
        styleHeader(alert, "#059669", "#10b981");   // greens
        alert.showAndWait();
    }

    /**
     * Shows a success alert with a custom title.
     */
    public static void showSuccess(String title, String message, Window owner) {
        Alert alert = build(AlertType.INFORMATION, title, null, message, owner);
        styleHeader(alert, "#059669", "#10b981");
        alert.showAndWait();
    }

    /**
     * Shows an error alert.
     *
     * @param message the body text (e.g. exception message)
     * @param owner   parent window (nullable)
     */
    public static void showError(String message, Window owner) {
        Alert alert = build(AlertType.ERROR, "⛔  Error", null, message, owner);
        styleHeader(alert, "#b91c1c", "#dc2626");   // reds
        alert.showAndWait();
    }

    /**
     * Shows an error alert with a custom title.
     */
    public static void showError(String title, String message, Window owner) {
        Alert alert = build(AlertType.ERROR, title, null, message, owner);
        styleHeader(alert, "#b91c1c", "#dc2626");
        alert.showAndWait();
    }

    /**
     * Shows a warning alert.
     */
    public static void showWarning(String message, Window owner) {
        Alert alert = build(AlertType.WARNING, "⚠  Warning", null, message, owner);
        styleHeader(alert, "#d97706", "#f59e0b");   // ambers
        alert.showAndWait();
    }

    /**
     * Shows a warning alert with a custom title.
     */
    public static void showWarning(String title, String message, Window owner) {
        Alert alert = build(AlertType.WARNING, title, null, message, owner);
        styleHeader(alert, "#d97706", "#f59e0b");
        alert.showAndWait();
    }

    /**
     * Shows a generic informational alert.
     */
    public static void showInfo(String message, Window owner) {
        Alert alert = build(AlertType.INFORMATION, "ℹ  Info", null, message, owner);
        styleHeader(alert, "#0369a1", "#0284c7");
        alert.showAndWait();
    }

    /**
     * Shows a generic informational alert with a custom title.
     */
    public static void showInfo(String title, String message, Window owner) {
        Alert alert = build(AlertType.INFORMATION, title, null, message, owner);
        styleHeader(alert, "#0369a1", "#0284c7");
        alert.showAndWait();
    }

    // ─────────────────────────────────────────────────────────
    // Confirmation dialogs
    // ─────────────────────────────────────────────────────────

    /**
     * Blocks until the user clicks OK or Cancel.
     *
     * @param title   dialog window title
     * @param message confirmation question to display
     * @param owner   parent window (nullable)
     * @return {@code true} if user pressed OK, {@code false} for Cancel / close
     */
    public static boolean confirm(String title, String message, Window owner) {
        Alert alert = build(AlertType.CONFIRMATION, title, null, message, owner);
        styleHeader(alert, "#6d28d9", "#7c3aed");   // purples
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Blocking confirmation with separate header and body texts.
     *
     * @param title  dialog title (window bar)
     * @param header bold header line below the title graphic (nullable)
     * @param body   detailed explanation body text
     * @param owner  parent window (nullable)
     * @return {@code true} if user clicked OK
     */
    public static boolean confirm(String title, String header, String body, Window owner) {
        Alert alert = build(AlertType.CONFIRMATION, title, header, body, owner);
        styleHeader(alert, "#6d28d9", "#7c3aed");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Destructive confirmation — uses a red header to communicate danger.
     *
     * @param title   dialog title
     * @param message confirmation question (e.g. "Delete this startup?")
     * @param owner   parent window
     * @return {@code true} if user clicked OK
     */
    public static boolean confirmDanger(String title, String message, Window owner) {
        Alert alert = build(AlertType.CONFIRMATION, title, null, message, owner);
        styleHeader(alert, "#b91c1c", "#dc2626");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // ─────────────────────────────────────────────────────────
    // Validation helpers
    // ─────────────────────────────────────────────────────────

    /**
     * Shows an error alert listing all validation errors collected by
     * {@link ValidationUtil}.  Each error is displayed as a bullet point.
     *
     * @param errors non-empty list of error strings
     * @param owner  parent window (nullable)
     */
    public static void showValidationErrors(List<String> errors, Window owner) {
        if (errors == null || errors.isEmpty()) return;
        String body = ValidationUtil.formatErrors(errors);
        Alert alert = build(AlertType.ERROR, "⛔  Validation Failed",
                "Please fix the following issues:", body, owner);
        styleHeader(alert, "#b91c1c", "#dc2626");
        // Allow the content label to wrap properly
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    /**
     * Convenience overload — shows validation errors if the list is non-empty,
     * and returns {@code true} if there were no errors (i.e. caller can proceed).
     *
     * <pre>
     *   if (!AlertUtil.checkAndShowErrors(errors, owner)) return;
     * </pre>
     */
    public static boolean checkAndShowErrors(List<String> errors, Window owner) {
        if (errors == null || errors.isEmpty()) return true;
        showValidationErrors(errors, owner);
        return false;
    }

    // ─────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────

    /**
     * Constructs a base Alert with consistent sizing, text wrapping, and optional
     * owner-window centering.
     */
    private static Alert build(AlertType type,
                                String title,
                                String header,
                                String content,
                                Window owner) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        DialogPane pane = alert.getDialogPane();

        // Inject the application stylesheet so CSS classes and .dark-dialog rules apply
        URL cssUrl = AlertUtil.class.getResource("/styles.css");
        if (cssUrl != null) {
            String cssExt = cssUrl.toExternalForm();
            if (!pane.getStylesheets().contains(cssExt)) {
                pane.getStylesheets().add(cssExt);
            }
        }
        // Apply light.css if the current theme is light
        ThemeManager.getInstance().applyTo(pane);

        pane.getStyleClass().add("dark-dialog");
        pane.setMinWidth(440);
        pane.setMinHeight(Region.USE_PREF_SIZE);

        if (owner != null) alert.initOwner(owner);
        return alert;
    }

    /**
     * Sets a gradient header background on the dialog's header panel.
     *
     * @param colorFrom CSS hex color at the gradient start
     * @param colorTo   CSS hex color at the gradient end
     */
    private static void styleHeader(Alert alert, String colorFrom, String colorTo) {
        alert.setOnShown(event -> {
            // Defer one pulse so CSS processing is complete before we query nodes
            Platform.runLater(() -> {
                DialogPane pane = alert.getDialogPane();

                // ── Force outer pane to gradient (beats Modena inline) ──
                pane.setStyle(DesignTokens.dialogPane());

                // ── Force intermediate containers transparent ──────────────
                pane.lookupAll(".content").forEach(n -> {
                    if (n instanceof javafx.scene.layout.Region r)
                        r.setStyle(DesignTokens.dialogContent());
                });
                pane.lookupAll(".graphic-container").forEach(n -> {
                    if (n instanceof javafx.scene.layout.Region r)
                        r.setStyle(DesignTokens.dialogGraphic());
                });

                // ── Content label — wrap and themed text ───────────────────
                Label lbl = (Label) pane.lookup(".content.label");
                if (lbl != null) {
                    lbl.setWrapText(true);
                    lbl.setMaxWidth(Double.MAX_VALUE);
                    lbl.setStyle(DesignTokens.dialogContentLabel());
                }

                // ── Header panel background ────────────────────────────────
                javafx.scene.Node headerPanel = pane.lookup(".header-panel");
                if (headerPanel != null) headerPanel.setStyle(
                    "-fx-background-color: linear-gradient(to right," + colorFrom + "," + colorTo + ");" +
                    "-fx-background-radius: 20 20 0 0;" +
                    "-fx-padding: 18 22 18 22;");

                // ── Header label — white bold ──────────────────────────────
                javafx.scene.Node headerLabel = pane.lookup(".header-panel .label");
                if (headerLabel != null) headerLabel.setStyle(DesignTokens.dialogHeaderLabel());

                // ── Button bar ────────────────────────────────────────────
                javafx.scene.Node bb = pane.lookup(".button-bar");
                if (bb instanceof javafx.scene.layout.Region r) r.setStyle(
                    DesignTokens.dialogButtonBar());

                // ── Buttons ───────────────────────────────────────────────
                pane.lookupAll(".button").forEach(node -> {
                    if (node instanceof javafx.scene.control.Button b) {
                        boolean isCancel = b.isCancelButton()
                            || b.getText().equalsIgnoreCase("cancel")
                            || b.getText().equalsIgnoreCase("close");
                        if (isCancel) {
                            b.setStyle(DesignTokens.btnCancel());
                        } else {
                            b.setStyle(DesignTokens.btnSave());
                        }
                    }
                });

                // ── Spring-in animation ────────────────────────────────────
                pane.setScaleX(0.86); pane.setScaleY(0.86); pane.setOpacity(0);
                javafx.animation.Interpolator spring = javafx.animation.Interpolator.SPLINE(0.22, 0.61, 0.36, 1.0);
                javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(280), pane);
                st.setFromX(0.86); st.setToX(1.0); st.setFromY(0.86); st.setToY(1.0); st.setInterpolator(spring);
                javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(220), pane);
                ft.setFromValue(0); ft.setToValue(1);
                new javafx.animation.ParallelTransition(st, ft).play();
            });
        });
    }
}

