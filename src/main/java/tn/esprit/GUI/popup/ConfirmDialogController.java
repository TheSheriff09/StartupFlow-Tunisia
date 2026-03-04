package tn.esprit.GUI.popup;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for confirmdialog.fxml.
 *
 * Three pre-built types:
 *   CONFIRM — purple gradient confirm button + cancel
 *   DANGER  — red gradient confirm button + cancel  (for deletes)
 *   MESSAGE — single OK button, no cancel          (errors / info)
 *
 * Use PopupManager to show this overlay rather than instantiating directly.
 */
public class ConfirmDialogController implements Initializable {

    public enum Type { CONFIRM, DANGER, MESSAGE }

    // ── FXML nodes ────────────────────────────────────────────
    @FXML private StackPane overlayRoot;
    @FXML private VBox      modalCard;
    @FXML private HBox      titleBar;
    @FXML private Label     iconLabel;
    @FXML private Label     titleLabel;
    @FXML private Label     messageLabel;
    @FXML private HBox      buttonBar;
    @FXML private Button    cancelBtn;
    @FXML private Button    confirmBtn;

    // ── Callbacks ─────────────────────────────────────────────
    private Runnable onConfirmCallback;
    private Runnable onCloseCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) { /* nothing to do */ }

    // ── Public API ────────────────────────────────────────────

    /**
     * Configure the dialog before calling {@link #animateOpen()}.
     *
     * @param type         CONFIRM, DANGER or MESSAGE
     * @param icon         Emoji shown in the title bar
     * @param title        Title bar text
     * @param message      Body message
     * @param confirmText  Text for the action button
     */
    public void configure(Type type, String icon, String title, String message, String confirmText) {
        iconLabel.setText(icon);
        titleLabel.setText(title);
        messageLabel.setText(message);
        confirmBtn.setText(confirmText);

        // Style title bar + confirm button according to type
        switch (type) {
            case DANGER -> {
                titleBar.getStyleClass().add("popup-title-bar-danger");
                confirmBtn.getStyleClass().setAll("popup-danger-btn");
            }
            case MESSAGE -> {
                // Hide cancel, centre single button
                cancelBtn.setVisible(false);
                cancelBtn.setManaged(false);
                buttonBar.setStyle("-fx-alignment: CENTER;");
                confirmBtn.getStyleClass().setAll("modal-save-btn");
            }
            default /* CONFIRM */ -> confirmBtn.getStyleClass().setAll("popup-confirm-btn");
        }
    }

    public void setOnConfirm(Runnable cb) { this.onConfirmCallback = cb; }
    public void setOnClose(Runnable cb)   { this.onCloseCallback   = cb; }

    // ── Animations ────────────────────────────────────────────

    /** Call after the overlay has been added to the scene graph. */
    public void animateOpen() {
        overlayRoot.setOpacity(0);
        modalCard.setOpacity(0);
        modalCard.setScaleX(0.82);
        modalCard.setScaleY(0.82);

        FadeTransition bgFt = new FadeTransition(Duration.millis(250), overlayRoot);
        bgFt.setFromValue(0); bgFt.setToValue(1);

        Interpolator spring = Interpolator.SPLINE(0.22, 0.61, 0.36, 1.0);

        ScaleTransition cardSt = new ScaleTransition(Duration.millis(320), modalCard);
        cardSt.setFromX(0.82); cardSt.setToX(1.0);
        cardSt.setFromY(0.82); cardSt.setToY(1.0);
        cardSt.setInterpolator(spring);

        FadeTransition cardFt = new FadeTransition(Duration.millis(250), modalCard);
        cardFt.setFromValue(0); cardFt.setToValue(1);

        new ParallelTransition(bgFt, cardSt, cardFt).play();
    }

    private void animateClose(Runnable afterClose) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), overlayRoot);
        ft.setFromValue(1); ft.setToValue(0);

        ScaleTransition st = new ScaleTransition(Duration.millis(200), modalCard);
        st.setToX(0.84); st.setToY(0.84);
        st.setInterpolator(Interpolator.EASE_IN);

        ParallelTransition pt = new ParallelTransition(ft, st);
        pt.setOnFinished(e -> { if (afterClose != null) afterClose.run(); });
        pt.play();
    }

    // ── FXML handlers ─────────────────────────────────────────

    @FXML
    private void onConfirm() {
        animateClose(() -> {
            if (onConfirmCallback != null) onConfirmCallback.run();
            if (onCloseCallback   != null) onCloseCallback.run();
        });
    }

    @FXML
    private void onCancel() {
        animateClose(() -> {
            if (onCloseCallback != null) onCloseCallback.run();
        });
    }
}

