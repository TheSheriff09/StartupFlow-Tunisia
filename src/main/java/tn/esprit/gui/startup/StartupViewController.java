package tn.esprit.gui.startup;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.Startup;
import tn.esprit.gui.businessplan.BusinessPlanViewController;
import tn.esprit.gui.popup.DialogStyler;
import tn.esprit.gui.popup.PopupManager;
import tn.esprit.services.StartupService;
import tn.esprit.utils.FormValidator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class StartupViewController implements Initializable {

    // ── FXML nodes ────────────────────────────────────────────
    @FXML private BorderPane mainContent;
    @FXML private StackPane  modalLayer;
    @FXML private FlowPane   cardsContainer;
    @FXML private TextField  searchField;
    @FXML private Label      lblCount;
    @FXML private Button     fabBtn;
    // ── Services ──────────────────────────────────────────────
    private final StartupService service = new StartupService();

    // ── State ─────────────────────────────────────────────────
    private List<Startup> allStartups;

    // ── Init ──────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterCards(newVal));
        loadAll();
        // Start FAB pulse animation after scene is ready
        javafx.application.Platform.runLater(this::breatheFAB);
    }

    /** Subtle idle "breathing" glow on the FAB. */
    private void breatheFAB() {
        if (fabBtn == null) return;
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(fabBtn.scaleXProperty(), 1.0),
                new KeyValue(fabBtn.scaleYProperty(), 1.0)),
            new KeyFrame(Duration.millis(1100),
                new KeyValue(fabBtn.scaleXProperty(), 1.07),
                new KeyValue(fabBtn.scaleYProperty(), 1.07)),
            new KeyFrame(Duration.millis(2200),
                new KeyValue(fabBtn.scaleXProperty(), 1.0),
                new KeyValue(fabBtn.scaleYProperty(), 1.0))
        );
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();
    }

    // ── Data ──────────────────────────────────────────────────

    private void loadAll() {
        allStartups = service.list();
        renderCards(allStartups);
    }

    private void filterCards(String query) {
        if (query == null || query.isBlank()) {
            renderCards(allStartups);
            return;
        }
        String q = query.toLowerCase().trim();
        List<Startup> filtered = allStartups.stream().filter(s ->
                (s.getName()        != null && s.getName().toLowerCase().contains(q)) ||
                (s.getSector()      != null && s.getSector().toLowerCase().contains(q)) ||
                (s.getDescription() != null && s.getDescription().toLowerCase().contains(q)) ||
                (s.getStage()       != null && s.getStage().toLowerCase().contains(q))
        ).collect(Collectors.toList());
        renderCards(filtered);
    }

    // ── Card rendering ────────────────────────────────────────

    private void renderCards(List<Startup> startups) {
        cardsContainer.getChildren().clear();
        lblCount.setText(startups.size() + " startup" + (startups.size() != 1 ? "s" : ""));

        for (int i = 0; i < startups.size(); i++) {
            Startup s = startups.get(i);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/startupcard.fxml"));
                Parent card = loader.load();
                StartupCardController ctrl = loader.getController();

                ctrl.setData(
                        s,
                        this::openEditDialog,
                        this::confirmDelete,
                        this::navigateToBusinessPlans
                );

                animateIn(card, i * 55L);
                cardsContainer.getChildren().add(card);

            } catch (IOException e) {
                System.err.println("[StartupViewController] Card load error: " + e.getMessage());
            }
        }
    }

    private void animateIn(Node node, long delayMs) {
        node.setOpacity(0);
        node.setScaleX(0.88);
        node.setScaleY(0.88);
        node.setTranslateY(18);

        FadeTransition ft = new FadeTransition(Duration.millis(420), node);
        ft.setFromValue(0); ft.setToValue(1);
        ft.setDelay(Duration.millis(delayMs));

        ScaleTransition st = new ScaleTransition(Duration.millis(420), node);
        st.setFromX(0.88); st.setToX(1.0);
        st.setFromY(0.88); st.setToY(1.0);
        st.setDelay(Duration.millis(delayMs));

        TranslateTransition tt = new TranslateTransition(Duration.millis(420), node);
        tt.setFromY(18); tt.setToY(0);
        tt.setDelay(Duration.millis(delayMs));

        ParallelTransition pt = new ParallelTransition(ft, st, tt);
        pt.play();
    }

    // ── FAB: open modern popup overlay ──────────────────────────

    @FXML
    private void openAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addstartupdialog.fxml"));
            Node overlay = loader.load();
            AddStartupController ctrl = loader.getController();

            // Wire save → persist + refresh
            ctrl.setOnSave(startup -> {
                service.add(startup);
                loadAll();
            });

            // Wire close → remove overlay, restore blur
            ctrl.setOnClose(() -> {
                modalLayer.getChildren().clear();
                modalLayer.setVisible(false);
                modalLayer.setManaged(false);
                if (mainContent != null) mainContent.setEffect(null);
            });

            // Inject overlay, blur background, animate in
            modalLayer.getChildren().setAll(overlay);
            modalLayer.setVisible(true);
            modalLayer.setManaged(true);
            if (mainContent != null) mainContent.setEffect(new GaussianBlur(7));
            ctrl.animateOpen();

        } catch (IOException e) {
            showError("Cannot open Add Startup dialog: " + e.getMessage());
        }
    }

    // ── Callbacks from cards ──────────────────────────────────

    private void openEditDialog(Startup s) {
        openStartupDialog(s);
    }

    private void confirmDelete(Startup s) {
        PopupManager.showDelete(
            modalLayer, mainContent,
            "Delete Startup",
            "Delete \"" + s.getName() + "\"? All linked business plans will also be deleted.",
            () -> { service.delete(s); loadAll(); });
    }

    private void navigateToBusinessPlans(Startup s) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/businessplanview.fxml"));
            Parent root = loader.load();
            BusinessPlanViewController ctrl = loader.getController();
            ctrl.initWithStartup(s);

            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setTitle("Business Plans — " + s.getName());
            stage.setScene(new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight()));
        } catch (IOException e) {
            showError("Cannot open Business Plans: " + e.getMessage());
        }
    }

    // ── Add / Edit dialog (programmatic, no extra FXML needed) ─

    private void openStartupDialog(Startup existing) {
        boolean isEdit = existing != null;

        // ── Form fields ──
        TextField   tfName      = styledField("e.g. EcoTech Solutions");
        TextField   tfSector    = styledField("e.g. FinTech / HealthTech");
        TextArea    taDesc      = styledArea("Brief description…");
        DatePicker  dpCreation  = styledDatePicker();
        TextField   tfStage     = styledField("e.g. Seed / Growth / Mature");
        ComboBox<String> cbStatus = styledCombo("Active", "Inactive", "Under Review", "Approved", "Scaling");
        TextField   tfFunding   = styledField("e.g. 50000");
        TextField   tfIncubator = styledField("e.g. StartupTN");

        // ── Error labels ──
        Label errName    = FormValidator.errorLabel();
        Label errFunding = FormValidator.errorLabel();

        // ── Real-time validation ──
        FormValidator.clearOnType(tfName, errName);
        FormValidator.enforceLengthLimit(tfName, 100);
        FormValidator.validateDoubleOnType(tfFunding, errFunding, false);
        FormValidator.enforceLengthLimit(tfIncubator, 120);

        // ── Image picker ──
        String[] imageHolder = { existing != null ? existing.getImageURL() : null };
        Label    errImage    = FormValidator.errorLabel();
        HBox     imageRow    = buildImagePicker(imageHolder, errImage, existing != null ? existing.getImageURL() : null);

        dpCreation.setValue(LocalDate.now());

        if (isEdit) {
            tfName.setText(existing.getName());
            tfSector.setText(existing.getSector());
            taDesc.setText(existing.getDescription());
            if (existing.getCreationDate() != null) dpCreation.setValue(existing.getCreationDate());
            tfStage.setText(existing.getStage());
            cbStatus.setValue(existing.getStatus() != null ? existing.getStatus() : "Active");
            if (existing.getFundingAmount() != null) tfFunding.setText(String.valueOf(existing.getFundingAmount()));
            tfIncubator.setText(existing.getIncubatorProgram());
        }

        // ── Layout ──
        Label secBasic   = sectionHeader("Basic Info");
        Label secDetails = sectionHeader("Details");
        Label secFinance = sectionHeader("Finance & Programme");

        VBox form = new VBox(8);
        form.setPadding(new Insets(6, 4, 6, 4));
        form.getChildren().addAll(
            secBasic,
            FormValidator.fieldRow("Name *",         tfName,      errName),
            FormValidator.fieldRow("Sector",          tfSector,    null),
            FormValidator.fieldRow("Description",     taDesc,      null),
            new VBox(5, DialogStyler.fieldLabel("Image"), imageRow, errImage),
            secDetails,
            FormValidator.fieldRow("Creation Date",   dpCreation,  null),
            FormValidator.fieldRow("Stage",            tfStage,     null),
            FormValidator.fieldRow("Status",           cbStatus,    null),
            secFinance,
            FormValidator.fieldRow("Funding (TND)",   tfFunding,   errFunding),
            FormValidator.fieldRow("Incubator",        tfIncubator, null)
        );

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(460);
        scroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        // ── Dialog ──
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Startup" : "New Startup");
        dialog.setHeaderText(isEdit ? "Edit \"" + existing.getName() + "\"" : "Create a new Startup");
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(500);
        DialogStyler.style(dialog);

        // Block dialog close if validation fails — errors shown inline
        dialog.getDialogPane().lookupButton(ButtonType.OK)
              .addEventFilter(ActionEvent.ACTION, ev -> {
                  boolean nameOk    = FormValidator.requireNonEmpty(tfName, errName);
                  boolean fundingOk = FormValidator.requireDouble(tfFunding, errFunding, false);
                  boolean imageOk   = FormValidator.requireImageExtension(imageHolder[0], errImage);
                  if (!nameOk || !fundingOk || !imageOk) ev.consume();
              });

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            // Validation already guaranteed by event filter
            Double funding = null;
            String fText = tfFunding.getText().trim();
            if (!fText.isBlank()) funding = Double.parseDouble(fText);

            if (isEdit) {
                existing.setName(tfName.getText().trim());
                existing.setSector(tfSector.getText().trim());
                existing.setDescription(taDesc.getText().trim());
                existing.setImageURL(imageHolder[0]);
                existing.setCreationDate(dpCreation.getValue());
                existing.setStage(tfStage.getText().trim());
                existing.setStatus(cbStatus.getValue());
                existing.setFundingAmount(funding);
                existing.setIncubatorProgram(tfIncubator.getText().trim());
                service.update(existing);
            } else {
                Startup s = new Startup();
                s.setName(tfName.getText().trim());
                s.setSector(tfSector.getText().trim());
                s.setDescription(taDesc.getText().trim());
                s.setImageURL(imageHolder[0]);
                s.setCreationDate(dpCreation.getValue());
                s.setStage(tfStage.getText().trim());
                s.setStatus(cbStatus.getValue());
                s.setFundingAmount(funding);
                s.setIncubatorProgram(tfIncubator.getText().trim());
                service.add(s);
            }
            loadAll();
        }
    }

    // ── Form builder helpers ──────────────────────────────────

    /** Section divider — delegates to shared DialogStyler. */
    private Label sectionHeader(String text) {
        return DialogStyler.sectionLabel(text);
    }

    /**
     * Builds an image-picker row: thumbnail preview + Choose/Change button + path label.
     * The selected path is written into {@code holder[0]} so the caller can read it after OK.
     */
    private HBox buildImagePicker(String[] holder, Label errLbl, String existingPath) {
        // Thumbnail
        ImageView preview = new ImageView();
        preview.setFitWidth(72);
        preview.setFitHeight(72);
        preview.setPreserveRatio(true);

        Label placeholder = new Label("📷");
        placeholder.setStyle("-fx-font-size: 26px; -fx-text-fill: rgba(196,181,253,0.82);");

        StackPane thumb = new StackPane(preview, placeholder);
        thumb.setPrefSize(84, 84);
        thumb.setMinSize(84, 84);
        thumb.setMaxSize(84, 84);
        thumb.setStyle(
            "-fx-background-color: rgba(237,233,254,0.50);" +
            "-fx-border-color: rgba(167,139,250,0.72);" +
            "-fx-border-radius: 14;" +
            "-fx-background-radius: 14;" +
            "-fx-border-width: 1.5;");

        // Load existing image if available
        if (existingPath != null && !existingPath.isBlank()) {
            try {
                File f = new File(existingPath);
                if (f.exists()) {
                    preview.setImage(new Image(f.toURI().toString()));
                    placeholder.setVisible(false);
                }
            } catch (Exception ignored) {}
        }

        // Path label
        Label pathLabel = new Label(existingPath != null && !existingPath.isBlank()
                ? new File(existingPath).getName() : "No image selected");
        pathLabel.setStyle(
            "-fx-font-size: 10.5px;" +
            "-fx-text-fill: rgba(196,181,253,0.92);" +
            "-fx-font-style: italic;");
        pathLabel.setWrapText(true);

        // Choose button
        Button btnChoose = new Button("📁  Choose Image");
        btnChoose.setStyle(
            "-fx-background-color: rgba(237,233,254,0.86);" +
            "-fx-border-color: rgba(167,139,250,0.72);" +
            "-fx-border-radius: 12;-fx-background-radius: 12;" +
            "-fx-border-width: 1.5;-fx-text-fill: #6d28d9;" +
            "-fx-font-size: 13px;-fx-font-weight: 800;" +
            "-fx-padding: 9 18 9 18;-fx-cursor: hand;");
        btnChoose.setMaxWidth(Double.MAX_VALUE);
        btnChoose.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choose Image");
            fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images (PNG, JPG, JPEG)", "*.png", "*.jpg", "*.jpeg"));
            File chosen = fc.showOpenDialog(btnChoose.getScene().getWindow());
            if (chosen != null) {
                holder[0] = chosen.getAbsolutePath();
                pathLabel.setText(chosen.getName());
                preview.setImage(new Image(chosen.toURI().toString()));
                placeholder.setVisible(false);
                FormValidator.requireImageExtension(holder[0], errLbl);
            }
        });

        VBox right = new VBox(8, btnChoose, pathLabel);
        right.setFillWidth(true);
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox row = new HBox(12, thumb, right);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle(DialogStyler.inputStyle());
        return tf;
    }

    private TextArea styledArea(String prompt) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt);
        ta.setPrefRowCount(3);
        ta.setWrapText(true);
        ta.setMaxWidth(Double.MAX_VALUE);
        ta.setStyle(DialogStyler.inputStyle());
        return ta;
    }

    private DatePicker styledDatePicker() {
        DatePicker dp = new DatePicker();
        dp.setMaxWidth(Double.MAX_VALUE);
        dp.setStyle(
            "-fx-background-color: rgba(255,255,255,0.90);" +
            "-fx-border-color: rgba(167,139,250,0.55);" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-border-width: 1.5;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 3 0 3 6;");
        return dp;
    }

    private ComboBox<String> styledCombo(String... items) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(items);
        cb.setValue(items[0]);
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setStyle(
            "-fx-background-color: rgba(255,255,255,0.90);" +
            "-fx-border-color: rgba(167,139,250,0.55);" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-border-width: 1.5;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 3 0 3 6;");
        return cb;
    }

    // ── Admin Dashboard entry point ───────────────────────────

    @FXML
    private void openAdminDashboard() {
        try {
            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/admindashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, stage.getScene().getWidth(),
                    stage.getScene().getHeight());
            stage.setScene(scene);
            stage.setTitle("StartupFlow — Admin Dashboard");
        } catch (IOException e) {
            showError("Cannot open Admin Dashboard: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        PopupManager.showError(modalLayer, mainContent, msg);
    }
}
