package tn.esprit.gui.startup;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.utils.FormValidator;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import tn.esprit.entities.Startup;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Controller for the modern "Add Startup" glassmorphism popup overlay.
 * Loaded by StartupViewController and injected into the scene's modalLayer.
 */
public class AddStartupController implements Initializable {

    // ── FXML nodes ────────────────────────────────────────────
    @FXML private StackPane          overlayRoot;
    @FXML private VBox               modalCard;
    @FXML private TextField          tfName;
    @FXML private TextArea           taDescription;
    @FXML private TextField          tfMentor;
    @FXML private TextField          tfFunding;
    @FXML private TextField          tfKpiScore;
    @FXML private TextField          tfIncubator;
    @FXML private DatePicker         dpCreationDate;
    @FXML private ComboBox<String>   cbSector;
    @FXML private ComboBox<String>   cbStage;
    @FXML private ComboBox<String>   cbStatus;

    // ── Image upload nodes ────────────────────────────────────
    @FXML private Button    btnSelectImage;
    @FXML private ImageView ivPreview;
    @FXML private Label     lblImagePath;
    @FXML private Label     lblImagePlaceholder;
    // ── Error labels (injected by FXML) ────────────────────────────
    @FXML private Label errName;
    @FXML private Label errFunding;
    @FXML private Label errKpi;
    @FXML private Label errImage;
    // ── State ─────────────────────────────────────────────────
    private String selectedImagePath = null;

    // ── Callbacks wired by the parent controller ──────────────
    private Consumer<Startup> onSaveCallback;
    private Runnable          onCloseCallback;

    // ── Init ──────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbSector.getItems().addAll(
            "FinTech", "HealthTech", "EdTech", "AgriTech",
            "E-Commerce", "CleanTech", "B2B SaaS", "Other");
        cbStage.getItems().addAll(
            "Idea", "MVP", "Seed", "Growth", "Mature", "Scaling");
        cbStatus.getItems().addAll(
            "Active", "Inactive", "Under Review", "Approved", "Scaling");

        cbSector.setValue("FinTech");
        cbStage.setValue("Seed");
        cbStatus.setValue("Active");

        // ── Real-time validation listeners ───────────────────────
        FormValidator.clearOnType(tfName, errName);
        FormValidator.enforceLengthLimit(tfName, 100);
        FormValidator.validateDoubleOnType(tfFunding, errFunding, false);
        FormValidator.validateDoubleOnType(tfKpiScore, errKpi, false);
        FormValidator.enforceLengthLimit(tfIncubator, 120);
        FormValidator.enforceLengthLimit(tfMentor, 20);
        // Disallow future creation dates
        dpCreationDate.valueProperty().addListener((obs, o, n) ->
            FormValidator.requireDate(dpCreationDate, null, true));
    }

    // ── Callback setters ──────────────────────────────────────

    public void setOnSave(Consumer<Startup> callback)  { this.onSaveCallback  = callback; }
    public void setOnClose(Runnable callback)           { this.onCloseCallback = callback; }

    // ── Open animation ────────────────────────────────────────

    /**
     * Call immediately after the overlay node has been added to the scene graph.
     * Plays a fade-in + scale-up spring animation on the card.
     */
    public void animateOpen() {
        overlayRoot.setOpacity(0);
        modalCard.setOpacity(0);
        modalCard.setScaleX(0.76);
        modalCard.setScaleY(0.76);
        modalCard.setTranslateY(28);

        // Overlay fade-in
        FadeTransition overlayFt = new FadeTransition(Duration.millis(260), overlayRoot);
        overlayFt.setFromValue(0);
        overlayFt.setToValue(1);

        // Card spring: scale + translate + fade
        Interpolator spring = Interpolator.SPLINE(0.22, 0.61, 0.36, 1.0);

        ScaleTransition cardSt = new ScaleTransition(Duration.millis(360), modalCard);
        cardSt.setFromX(0.76); cardSt.setToX(1.0);
        cardSt.setFromY(0.76); cardSt.setToY(1.0);
        cardSt.setInterpolator(spring);

        TranslateTransition cardTt = new TranslateTransition(Duration.millis(360), modalCard);
        cardTt.setFromY(28); cardTt.setToY(0);
        cardTt.setInterpolator(spring);

        FadeTransition cardFt = new FadeTransition(Duration.millis(310), modalCard);
        cardFt.setFromValue(0); cardFt.setToValue(1);

        new ParallelTransition(
            overlayFt,
            new ParallelTransition(cardSt, cardTt, cardFt)
        ).play();
    }

    // ── Close animation ───────────────────────────────────────

    private void animateClose(Runnable afterClose) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), overlayRoot);
        ft.setFromValue(1);
        ft.setToValue(0);

        ScaleTransition st = new ScaleTransition(Duration.millis(200), modalCard);
        st.setToX(0.84);
        st.setToY(0.84);
        st.setInterpolator(Interpolator.EASE_IN);

        ParallelTransition pt = new ParallelTransition(ft, st);
        pt.setOnFinished(e -> { if (afterClose != null) afterClose.run(); });
        pt.play();
    }

    // ── FXML event handlers ───────────────────────────────────

    @FXML
    private void onSelectImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Startup Image");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images (PNG, JPG, JPEG)",
                "*.png", "*.jpg", "*.jpeg")
        );
        File file = fc.showOpenDialog(btnSelectImage.getScene().getWindow());
        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            lblImagePath.setText(file.getName());
            if (lblImagePlaceholder != null) lblImagePlaceholder.setVisible(false);
            ivPreview.setImage(new Image(file.toURI().toString()));
            FormValidator.requireImageExtension(selectedImagePath, errImage);
        }
    }

    @FXML
    private void onCancel() {
        animateClose(() -> {
            if (onCloseCallback != null) onCloseCallback.run();
        });
    }

    @FXML
    private void onSave() {
        // ── Validate before save ─────────────────────────────────
        boolean nameOk    = FormValidator.requireNonEmpty(tfName, errName);
        boolean fundingOk = FormValidator.requireDouble(tfFunding, errFunding, false);
        boolean kpiOk     = FormValidator.requireKpiScore(tfKpiScore, errKpi);
        boolean imageOk   = FormValidator.requireImageExtension(selectedImagePath, errImage);

        if (!nameOk || !fundingOk || !kpiOk || !imageOk) {
            if (!nameOk) shakeNode(tfName);
            return;
        }

        Startup s = new Startup();
        s.setName(tfName.getText().trim());
        s.setSector(cbSector.getValue());
        s.setStage(cbStage.getValue());
        s.setStatus(cbStatus.getValue());
        if (selectedImagePath != null) s.setImageURL(selectedImagePath);

        String desc = taDescription.getText().trim();
        if (!desc.isEmpty()) s.setDescription(desc);

        String fundingText = tfFunding.getText().trim();
        if (!fundingText.isEmpty()) s.setFundingAmount(Double.parseDouble(fundingText));

        String kpiText = tfKpiScore.getText().trim();
        if (!kpiText.isEmpty()) s.setKpiScore(Double.parseDouble(kpiText));

        String incubator = tfIncubator.getText().trim();
        if (!incubator.isEmpty()) s.setIncubatorProgram(incubator);

        if (dpCreationDate.getValue() != null)
            s.setCreationDate(dpCreationDate.getValue());

        String mentorText = tfMentor.getText().trim();
        if (!mentorText.isEmpty()) {
            try { s.setMentorID(Integer.parseInt(mentorText)); }
            catch (NumberFormatException ignored) { /* leave mentorID null */ }
        }

        animateClose(() -> {
            if (onSaveCallback  != null) onSaveCallback.accept(s);
            if (onCloseCallback != null) onCloseCallback.run();
        });
    }

    // ── Helpers ───────────────────────────────────────────────

    /** Horizontal shake on required-field validation failure. */
    private void shakeNode(Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(55), node);
        shake.setFromX(0);
        shake.setByX(9);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }
}
