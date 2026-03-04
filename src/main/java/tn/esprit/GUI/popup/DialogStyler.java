package tn.esprit.GUI.popup;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import tn.esprit.utils.DesignTokens;
import tn.esprit.utils.ThemeManager;

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

    // ── All design tokens now sourced from DesignTokens.java ───

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
        // Apply light.css if the current theme is light
        ThemeManager.getInstance().applyTo(pane);

        // setOnShown: handle things CSS cannot decide at authoring time
        dialog.setOnShown(event -> {
            // Defer one pulse so CSS has finished applying before we override
            Platform.runLater(() -> {

                // ── 1. Force the outer DialogPane to the dark gradient ────
                // Modena's .dialog-pane background can override our CSS class;
                // an inline style beats all class selectors reliably.
                pane.setStyle(DesignTokens.dialogPane());

                // ── 2. Force intermediate containers transparent ──────────
                pane.lookupAll(".content").forEach(n -> {
                    if (n instanceof Region r)
                        r.setStyle(DesignTokens.dialogContent());
                });
                pane.lookupAll(".graphic-container").forEach(n -> {
                    if (n instanceof Region r)
                        r.setStyle(DesignTokens.dialogGraphic());
                });

                // ── 3. Re-style header panel ──────────────────────────────
                javafx.scene.Node hp = pane.lookup(".header-panel");
                if (hp != null) hp.setStyle(
                    DesignTokens.headerPanelStyle("#3b1276,#6d28d9,#9333ea", "#c026d3"));

                javafx.scene.Node hpLabel = pane.lookup(".header-panel .label");
                if (hpLabel != null) hpLabel.setStyle(DesignTokens.dialogHeaderLabel());

                // ── 4. Button bar ─────────────────────────────────────────
                javafx.scene.Node bb = pane.lookup(".button-bar");
                if (bb instanceof Region r) r.setStyle(DesignTokens.dialogButtonBar());

                // ── 5. OK vs Cancel button distinction ────────────────────
                pane.lookupAll(".button").forEach(node -> {
                    if (node instanceof Button b) {
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

                // ── 6. Spring open animation ──────────────────────────────
                pane.setScaleX(DesignTokens.ANIM_OPEN_SCALE);
                pane.setScaleY(DesignTokens.ANIM_OPEN_SCALE);
                pane.setOpacity(0);
                Interpolator spring = Interpolator.SPLINE(0.22, 0.61, 0.36, 1.0);

                ScaleTransition st = new ScaleTransition(
                    Duration.millis(DesignTokens.ANIM_OPEN_SCALE_MS), pane);
                st.setFromX(DesignTokens.ANIM_OPEN_SCALE);
                st.setToX(1.0);
                st.setFromY(DesignTokens.ANIM_OPEN_SCALE);
                st.setToY(1.0);
                st.setInterpolator(spring);

                FadeTransition ft = new FadeTransition(
                    Duration.millis(DesignTokens.ANIM_OPEN_FADE_MS), pane);
                ft.setFromValue(0.0); ft.setToValue(1.0);

                new ParallelTransition(st, ft).play();
            });
        });
    }

    // ── Shared form-builder helpers ───────────────────────────

    /** Field label for use inside glass-card dialogs. */
    public static Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(DesignTokens.fieldLabel());
        return lbl;
    }

    /** Section header separator. */
    public static Label sectionLabel(String text) {
        Label lbl = new Label(text.toUpperCase());
        lbl.setStyle(DesignTokens.sectionLabel());
        return lbl;
    }

    /** Input field inline style — theme-aware. */
    public static String inputStyle() { return DesignTokens.fieldNormal(); }
}

