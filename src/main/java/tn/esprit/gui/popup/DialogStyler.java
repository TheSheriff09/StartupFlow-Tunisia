package tn.esprit.gui.popup;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * DialogStyler — Central styling utility for all JavaFX Dialog windows.
 *
 * Applies the dark-glass theme by adding the {@code .dark-dialog} CSS class
 * (defined in styles.css) to the DialogPane.  This guarantees higher
 * specificity than Modena, so the content area and every inner node get
 * styled correctly without fragile runtime lookups.
 *
 * Only {@code setOnShown} logic remains for things CSS cannot decide at
 * authoring time (OK vs Cancel button distinction, spring animation).
 *
 * Usage: {@code DialogStyler.style(dialog);} before {@code showAndWait()}.
 */
public final class DialogStyler {

    // ── Design tokens (mirrors .dark-dialog in styles.css) ───
    private static final String BTN_SAVE  =
        "linear-gradient(to right, #7c3aed, #a855f7, #c026d3)";

    private static final String FIELD_STYLE =
        "-fx-background-color: rgba(255,255,255,0.90);" +
        "-fx-text-fill: #3b1f6b;" +
        "-fx-border-color: rgba(167,139,250,0.55);" +
        "-fx-border-radius: 12;" +
        "-fx-background-radius: 12;" +
        "-fx-border-width: 1.5;" +
        "-fx-prompt-text-fill: rgba(139,92,246,0.48);" +
        "-fx-font-size: 13px;" +
        "-fx-padding: 11 16 11 16;";

    private DialogStyler() {}

    /**
     * Apply the unified glass-card theme to a dialog and add a spring open animation.
     * Call this BEFORE {@code dialog.showAndWait()}.
     */
    public static void style(Dialog<?> dialog) {
        DialogPane pane = dialog.getDialogPane();

        // Add .dark-dialog CSS class — all structural styling is done via CSS rules
        // in styles.css under .dark-dialog, which beats Modena specificity reliably.
        if (!pane.getStyleClass().contains("dark-dialog")) {
            pane.getStyleClass().add("dark-dialog");
        }

        // Attach our stylesheet so the .dark-dialog rules are available in the dialog scene
        String css = DialogStyler.class.getResource("/styles.css").toExternalForm();
        if (!pane.getStylesheets().contains(css)) {
            pane.getStylesheets().add(css);
        }

        // setOnShown: handle things CSS cannot decide at authoring time
        dialog.setOnShown(event -> {

            // ── OK vs Cancel button distinction ──────────────
            // CSS gives all buttons gradient; Cancel needs ghost style.
            pane.lookupAll(".button").forEach(node -> {
                if (node instanceof Button b) {
                    boolean isCancel = b.isCancelButton()
                        || b.getText().equalsIgnoreCase("cancel")
                        || b.getText().equalsIgnoreCase("close");
                    if (isCancel) {
                        b.setStyle(
                            "-fx-background-color: rgba(255,255,255,0.10);" +
                            "-fx-border-color: rgba(255,255,255,0.38);" +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 20;" +
                            "-fx-background-radius: 20;" +
                            "-fx-text-fill: rgba(255,255,255,0.82);" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 10 26 10 26;" +
                            "-fx-cursor: hand;");
                    } else {
                        b.setStyle(
                            "-fx-background-color: " + BTN_SAVE + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: transparent;" +
                            "-fx-padding: 10 28 10 28;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian,rgba(192,38,211,0.65),18,0.12,0,5);");
                    }
                }
            });

            // ── Spring open animation ─────────────────────────
            pane.setScaleX(0.84); pane.setScaleY(0.84); pane.setOpacity(0);
            Interpolator spring = Interpolator.SPLINE(0.22, 0.61, 0.36, 1.0);

            ScaleTransition st = new ScaleTransition(Duration.millis(300), pane);
            st.setFromX(0.84); st.setToX(1.0);
            st.setFromY(0.84); st.setToY(1.0);
            st.setInterpolator(spring);

            FadeTransition ft = new FadeTransition(Duration.millis(250), pane);
            ft.setFromValue(0.0); ft.setToValue(1.0);

            new ParallelTransition(st, ft).play();
        });
    }

    // ── Shared form-builder helpers ───────────────────────────

    /** Field label for use inside glass-card dialogs (light text on dark bg). */
    public static Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(
            "-fx-text-fill: rgba(233,213,255,0.85);" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;");
        return lbl;
    }

    /** Section header separator — dimmer uppercase label. */
    public static Label sectionLabel(String text) {
        Label lbl = new Label(text.toUpperCase());
        lbl.setStyle(
            "-fx-text-fill: rgba(216,180,254,0.55);" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 14 0 4 2;" +
            "-fx-border-color: transparent transparent rgba(255,255,255,0.12) transparent;" +
            "-fx-border-width: 0 0 1 0;" +
            "-fx-pref-width: 10000;");
        return lbl;
    }

    /** Input field inline style — translucent white on dark card. */
    public static String inputStyle() { return FIELD_STYLE; }
}
