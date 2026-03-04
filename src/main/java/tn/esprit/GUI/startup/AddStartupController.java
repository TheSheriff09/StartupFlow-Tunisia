package tn.esprit.GUI.startup;

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
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tn.esprit.entities.Startup;
import tn.esprit.Services.StartupService;
import tn.esprit.utils.AlertUtil;
import tn.esprit.utils.FormValidator;
import tn.esprit.utils.ValidationUtil;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Controller for the modern "Add Startup" glassmorphism popup overlay.
 * Loaded by StartupViewController and injected into the scene's modalLayer.
 */
public class AddStartupController implements Initializable {

    // ── FXML nodes ────────────────────────────────────────────
    @FXML
    private StackPane overlayRoot;
    @FXML
    private VBox modalCard;
    @FXML
    private TextField tfName;
    @FXML
    private TextArea taDescription;
    @FXML
    private TextField tfMentor;
    @FXML
    private TextField tfFunding;
    @FXML
    private TextField tfKpiScore;
    @FXML
    private TextField tfIncubator;
    @FXML
    private DatePicker dpCreationDate;
    @FXML
    private ComboBox<String> cbSector;
    @FXML
    private ComboBox<String> cbStage;
    @FXML
    private ComboBox<String> cbStatus;

    // ── Image upload nodes ────────────────────────────────────
    @FXML
    private Button btnSelectImage;
    @FXML
    private ImageView ivPreview;
    @FXML
    private Label lblImagePath;
    @FXML
    private Label lblImagePlaceholder;
    // ── Error labels (injected by FXML) ────────────────────────────
    @FXML
    private Label errName;
    @FXML
    private Label errFunding;
    @FXML
    private Label errKpi;
    @FXML
    private Label errImage;
    @FXML
    private Label errMentor;
    // ── State ─────────────────────────────────────────────────
    private String selectedImagePath = null;

    /** Used for duplicate-name detection at save time. */
    private final StartupService startupService = new StartupService();

    // ── Callbacks wired by the parent controller ──────────────
    private Consumer<Startup> onSaveCallback;
    private Runnable onCloseCallback;

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
        // Mentor ID: numeric-only input enforcement (block non-digit characters)
        tfMentor.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                tfMentor.setText(newVal.replaceAll("\\D", ""));
            }
            if (errMentor != null) {
                if (newVal != null && !newVal.isBlank()) {
                    try {
                        int v = Integer.parseInt(newVal.trim());
                        if (v <= 0)
                            showInlineError(errMentor, "Mentor ID must be a positive integer.");
                        else
                            hideInlineError(errMentor);
                    } catch (NumberFormatException ex) {
                        hideInlineError(errMentor);
                    }
                } else {
                    hideInlineError(errMentor);
                }
            }
        });
        // Disallow future creation dates
        dpCreationDate.valueProperty()
                .addListener((obs, o, n) -> FormValidator.requireDate(dpCreationDate, null, true));

        // ── Tooltips on action buttons ───────────────────────────
        btnSelectImage.setTooltip(new Tooltip("Upload a PNG, JPG, or JPEG image for this startup"));
    }

    // ── Callback setters ──────────────────────────────────────

    public void setOnSave(Consumer<Startup> callback) {
        this.onSaveCallback = callback;
    }

    public void setOnClose(Runnable callback) {
        this.onCloseCallback = callback;
    }

    // ── Open animation ────────────────────────────────────────

    /**
     * Call immediately after the overlay node has been added to the scene graph.
     * Plays a fade-in + scale-up spring animation on the card.
     */
    public void animateOpen() {
        overlayRoot.setOpacity(0);
        modalCard.setOpacity(0);
        modalCard.setScaleX(0.82);
        modalCard.setScaleY(0.82);
        modalCard.setTranslateY(28);

        // Overlay fade-in
        FadeTransition overlayFt = new FadeTransition(Duration.millis(250), overlayRoot);
        overlayFt.setFromValue(0);
        overlayFt.setToValue(1);

        // Card spring: scale + translate + fade
        Interpolator spring = Interpolator.SPLINE(0.22, 0.61, 0.36, 1.0);

        ScaleTransition cardSt = new ScaleTransition(Duration.millis(320), modalCard);
        cardSt.setFromX(0.82);
        cardSt.setToX(1.0);
        cardSt.setFromY(0.82);
        cardSt.setToY(1.0);
        cardSt.setInterpolator(spring);

        TranslateTransition cardTt = new TranslateTransition(Duration.millis(320), modalCard);
        cardTt.setFromY(28);
        cardTt.setToY(0);
        cardTt.setInterpolator(spring);

        FadeTransition cardFt = new FadeTransition(Duration.millis(250), modalCard);
        cardFt.setFromValue(0);
        cardFt.setToValue(1);

        new ParallelTransition(
                overlayFt,
                new ParallelTransition(cardSt, cardTt, cardFt)).play();
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
        pt.setOnFinished(e -> {
            if (afterClose != null)
                afterClose.run();
        });
        pt.play();
    }

    // ── FXML event handlers ───────────────────────────────────

    @FXML
    private void onSelectImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Startup Image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images (PNG, JPG, JPEG)",
                        "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog(btnSelectImage.getScene().getWindow());
        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            lblImagePath.setText(file.getName());
            if (lblImagePlaceholder != null)
                lblImagePlaceholder.setVisible(false);
            ivPreview.setImage(new Image(file.toURI().toString()));
            FormValidator.requireImageExtension(selectedImagePath, errImage);
        }
    }

    @FXML
    private void onCancel() {
        animateClose(() -> {
            if (onCloseCallback != null)
                onCloseCallback.run();
        });
    }

    @FXML
    private void onSave() {
        // ── Collect strict validation errors ──────────────────────
        java.util.List<String> errors = new java.util.ArrayList<>();

        // Fetch existing names for duplicate-name check
        java.util.List<String> existingNames = startupService.list().stream()
                .map(Startup::getName)
                .collect(java.util.stream.Collectors.toList());

        // Name: required, min 3, max 100, not only-digits, no duplicate
        ValidationUtil.collect(
                ValidationUtil.checkName(tfName.getText(), "Startup Name", existingNames, null),
                errors);

        // Mentor ID: optional but must be a positive integer if provided
        String mentorRaw = tfMentor.getText() == null ? "" : tfMentor.getText().trim();
        if (!mentorRaw.isEmpty()) {
            try {
                int mid = Integer.parseInt(mentorRaw);
                if (mid <= 0)
                    errors.add("Mentor ID must be a positive integer.");
            } catch (NumberFormatException ex) {
                errors.add("Mentor ID must be a valid integer.");
            }
        }

        // Funding: optional but must be > 0 if provided
        ValidationUtil.collect(ValidationUtil.checkFunding(tfFunding.getText()), errors);

        // KPI Score: optional, 0–10
        ValidationUtil.collect(ValidationUtil.checkKpiScore(tfKpiScore.getText()), errors);

        // Image extension: optional but must be PNG/JPG/JPEG if supplied
        boolean imageOk = FormValidator.requireImageExtension(selectedImagePath, errImage);
        if (!imageOk)
            errors.add("Image file must be PNG, JPG, or JPEG.");

        // ── Apply field-level red-border highlights ───────────────
        ValidationUtil.checkName(tfName.getText(), "Startup Name", existingNames, null)
                .ifPresentOrElse(
                        msg -> ValidationUtil.markFieldError(tfName, errName, msg),
                        () -> ValidationUtil.clearFieldError(tfName, errName));

        ValidationUtil.checkFunding(tfFunding.getText())
                .ifPresentOrElse(
                        msg -> ValidationUtil.markFieldError(tfFunding, errFunding, msg),
                        () -> ValidationUtil.clearFieldError(tfFunding, errFunding));

        ValidationUtil.checkKpiScore(tfKpiScore.getText())
                .ifPresentOrElse(
                        msg -> ValidationUtil.markFieldError(tfKpiScore, errKpi, msg),
                        () -> ValidationUtil.clearFieldError(tfKpiScore, errKpi));

        // Mentor ID highlight
        String mentorHighlight = tfMentor.getText() == null ? "" : tfMentor.getText().trim();
        if (!mentorHighlight.isEmpty()) {
            try {
                int mid = Integer.parseInt(mentorHighlight);
                if (mid <= 0)
                    ValidationUtil.markFieldError(tfMentor, errMentor, "Mentor ID must be a positive integer.");
                else
                    ValidationUtil.clearFieldError(tfMentor, errMentor);
            } catch (NumberFormatException ex) {
                ValidationUtil.markFieldError(tfMentor, errMentor, "Mentor ID must be a valid integer.");
            }
        } else {
            ValidationUtil.clearFieldError(tfMentor, errMentor);
        }
        // ── Stop and show bulleted error summary ──────────────────
        if (!errors.isEmpty()) {
            AlertUtil.showValidationErrors(errors,
                    overlayRoot.getScene() != null ? overlayRoot.getScene().getWindow() : null);
            shakeNode(tfName);
            return;
        }

        // ── Build Startup entity ──────────────────────────────────
        Startup s = new Startup();
        s.setName(tfName.getText().trim());
        s.setSector(cbSector.getValue());
        s.setStage(cbStage.getValue());
        s.setStatus(cbStatus.getValue());
        if (selectedImagePath != null)
            s.setImageURL(selectedImagePath);

        String desc = taDescription.getText().trim();
        if (!desc.isEmpty())
            s.setDescription(desc);

        String fundingText = tfFunding.getText().trim();
        if (!fundingText.isEmpty())
            s.setFundingAmount(Double.parseDouble(fundingText));

        String kpiText = tfKpiScore.getText().trim();
        if (!kpiText.isEmpty())
            s.setKpiScore(Double.parseDouble(kpiText));

        String incubator = tfIncubator.getText().trim();
        if (!incubator.isEmpty())
            s.setIncubatorProgram(incubator);

        if (dpCreationDate.getValue() != null)
            s.setCreationDate(dpCreationDate.getValue());

        if (tn.esprit.utils.SessionManager.getUser() != null) {
            s.setUserId(tn.esprit.utils.SessionManager.getUser().getId());
        }

        String mentorText = tfMentor.getText().trim();
        if (!mentorText.isEmpty()) {
            try {
                s.setMentorID(Integer.parseInt(mentorText));
            } catch (NumberFormatException ignored) {
                /* leave mentorID null */ }
        }

        animateClose(() -> {
            if (onSaveCallback != null)
                onSaveCallback.accept(s);
            if (onCloseCallback != null)
                onCloseCallback.run();
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

    private void showInlineError(Label lbl, String msg) {
        if (lbl == null)
            return;
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void hideInlineError(Label lbl) {
        if (lbl == null)
            return;
        lbl.setVisible(false);
        lbl.setManaged(false);
    }
}
