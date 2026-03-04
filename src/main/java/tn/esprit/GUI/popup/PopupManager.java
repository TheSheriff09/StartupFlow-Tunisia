package tn.esprit.GUI.popup;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.io.IOException;

/**
 * PopupManager — Static utility for the StartupFlow UI.
 *
 * Replaces ALL default JavaFX Alerts with custom glass-morphism overlays.
 * Every popup is injected into the scene's existing "modalLayer" StackPane so
 * the background stays visible (with Gaussian blur) instead of a separate window.
 *
 * Usage:
 * <pre>
 *   // Delete confirmation
 *   PopupManager.showDelete(modalLayer, mainContent,
 *       "Delete \"" + name + "\"?",
 *       "This action cannot be undone.",
 *       () -> { service.delete(item); loadAll(); });
 *
 *   // Error
 *   PopupManager.showError(modalLayer, mainContent, "Name is required.");
 *
 *   // Success notice
 *   PopupManager.showSuccess(modalLayer, mainContent, "Startup saved.");
 * </pre>
 */
public final class PopupManager {

    private PopupManager() { /* utility class */ }

    // ── Public convenience methods ────────────────────────────

    /**
     * Show a generic CONFIRM dialog (purple gradient action button).
     */
    public static void showConfirm(StackPane modalLayer, Region blurTarget,
                                   String title, String message,
                                   String confirmLabel, Runnable onConfirm) {
        show(modalLayer, blurTarget,
             ConfirmDialogController.Type.CONFIRM,
             "✦", title, message, confirmLabel,
             onConfirm);
    }

    /**
     * Show a DELETE confirmation dialog (red gradient action button).
     */
    public static void showDelete(StackPane modalLayer, Region blurTarget,
                                  String title, String message,
                                  Runnable onConfirm) {
        show(modalLayer, blurTarget,
             ConfirmDialogController.Type.DANGER,
             "🗑", title, message, "Delete",
             onConfirm);
    }

    /**
     * Show an ERROR message (single OK button, red title bar).
     */
    public static void showError(StackPane modalLayer, Region blurTarget, String message) {
        show(modalLayer, blurTarget,
             ConfirmDialogController.Type.DANGER,
             "✕", "Error", message, "OK",
             null);
    }

    /**
     * Show a SUCCESS notice (single OK button, purple title bar).
     */
    public static void showSuccess(StackPane modalLayer, Region blurTarget, String message) {
        show(modalLayer, blurTarget,
             ConfirmDialogController.Type.MESSAGE,
             "✓", "Success", message, "OK",
             null);
    }

    // ── Core loader ───────────────────────────────────────────

    private static void show(StackPane modalLayer,
                              Region    blurTarget,
                              ConfirmDialogController.Type type,
                              String icon, String title,
                              String message, String confirmLabel,
                              Runnable onConfirm) {
        try {
            FXMLLoader loader = new FXMLLoader(
                PopupManager.class.getResource("/confirmdialog.fxml"));
            Node overlay = loader.load();
            ConfirmDialogController ctrl = loader.getController();

            // Configure appearance
            ctrl.configure(type, icon, title, message, confirmLabel);
            ctrl.setOnConfirm(onConfirm);

            // Close handler — remove overlay and restore blur
            ctrl.setOnClose(() -> dismiss(modalLayer, blurTarget));

            // Inject, blur, animate
            modalLayer.getChildren().setAll(overlay);
            modalLayer.setVisible(true);
            modalLayer.setManaged(true);
            if (blurTarget != null) blurTarget.setEffect(new GaussianBlur(7));

            ctrl.animateOpen();

        } catch (IOException ex) {
            // Extreme fallback — should never happen in production
            System.err.println("[PopupManager] Failed to load confirmdialog.fxml: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /** Remove the overlay and clear the blur. Called by the dialog after close animation. */
    private static void dismiss(StackPane modalLayer, Region blurTarget) {
        modalLayer.getChildren().clear();
        modalLayer.setVisible(false);
        modalLayer.setManaged(false);
        if (blurTarget != null) blurTarget.setEffect(null);
    }
}

