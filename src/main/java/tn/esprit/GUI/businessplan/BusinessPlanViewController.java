package tn.esprit.GUI.businessplan;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.BusinessPlan;
import tn.esprit.entities.Startup;
import tn.esprit.GUI.popup.DialogStyler;
import tn.esprit.GUI.popup.PopupManager;
import tn.esprit.Services.BusinessPlanService;
import tn.esprit.utils.AlertUtil;
import tn.esprit.utils.DesignTokens;
import tn.esprit.utils.FormValidator;
import tn.esprit.utils.ThemeManager;
import tn.esprit.utils.ValidationUtil;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BusinessPlanViewController implements Initializable {

    // ── FXML nodes ────────────────────────────────────────────
    @FXML
    private BorderPane mainContent;
    @FXML
    private StackPane modalLayer;
    @FXML
    private FlowPane cardsContainer;
    @FXML
    private TextField searchField;
    @FXML
    private Label lblStartupName;
    @FXML
    private Label lblCount;
    @FXML
    private Button fabBtn;
    @FXML
    private Button themeToggle;
    @FXML
    private ComboBox<String> sortCombo;
    // ── Services ──────────────────────────────────────────────
    private final BusinessPlanService service = new BusinessPlanService();

    // ── State ─────────────────────────────────────────────────
    private Startup currentStartup;
    private List<BusinessPlan> allPlans;

    // ── Init ──────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchField.textProperty().addListener((obs, o, n) -> filterCards(n));

        // Sort options
        if (sortCombo != null) {
            sortCombo.getItems().addAll(
                    "Date (Newest)", "Date (Oldest)",
                    "Title (A → Z)", "Title (Z → A)",
                    "Funding (High → Low)", "Funding (Low → High)",
                    "Status");
        }

        // Set initial theme toggle icon
        if (themeToggle != null)
            themeToggle.setText(ThemeManager.getInstance().isDark() ? "☀" : "🌙");

        javafx.application.Platform.runLater(this::breatheFAB);
    }

    /** Subtle idle "breathing" glow on the FAB. */
    private void breatheFAB() {
        if (fabBtn == null)
            return;
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(fabBtn.scaleXProperty(), 1.0),
                        new KeyValue(fabBtn.scaleYProperty(), 1.0)),
                new KeyFrame(Duration.seconds(1.2),
                        new KeyValue(fabBtn.scaleXProperty(), 1.08),
                        new KeyValue(fabBtn.scaleYProperty(), 1.08)),
                new KeyFrame(Duration.seconds(2.4),
                        new KeyValue(fabBtn.scaleXProperty(), 1.0),
                        new KeyValue(fabBtn.scaleYProperty(), 1.0)));
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();
    }

    // ── Callbacks ─────────────────────────────────────────────
    @FXML
    private void toggleTheme() {
        ThemeManager tm = ThemeManager.getInstance();
        tm.toggle();
        tm.applyTo(themeToggle.getScene());
        themeToggle.setText(tm.isDark() ? "☀" : "🌙");
        // Re-render cards so inline styles update
        if (allPlans != null)
            renderCards(allPlans);
    }

    /**
     * Called by StartupViewController before showing this scene.
     * Injects the selected startup so we can load its plans.
     */
    public void initWithStartup(Startup startup) {
        this.currentStartup = startup;
        lblStartupName.setText("📋  " + startup.getName() + " — Business Plans");
        loadAll();
    }

    // ── Data ──────────────────────────────────────────────────

    private void loadAll() {
        allPlans = service.getByStartup(currentStartup.getStartupID()).stream()
                .sorted(Comparator
                        .comparing((BusinessPlan p) -> p.getLastUpdate() != null
                                ? p.getLastUpdate()
                                : p.getCreationDate(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed()
                        .thenComparing(BusinessPlan::getBusinessPlanID, Comparator.reverseOrder()))
                .collect(Collectors.toList());
        renderCards(allPlans);
    }

    // ── Sort handler ──────────────────────────────────────────

    @FXML
    private void onSortChanged() {
        if (sortCombo == null || sortCombo.getValue() == null || allPlans == null)
            return;
        String sort = sortCombo.getValue();
        Comparator<BusinessPlan> cmp = switch (sort) {
            case "Title (A → Z)" -> Comparator.comparing(p -> p.getTitle() != null ? p.getTitle().toLowerCase() : "");
            case "Title (Z → A)" -> Comparator
                    .comparing((BusinessPlan p) -> p.getTitle() != null ? p.getTitle().toLowerCase() : "").reversed();
            case "Funding (High → Low)" ->
                Comparator.comparing((BusinessPlan p) -> p.getFundingRequired() != null ? p.getFundingRequired() : 0.0,
                        Comparator.reverseOrder());
            case "Funding (Low → High)" ->
                Comparator.comparing((BusinessPlan p) -> p.getFundingRequired() != null ? p.getFundingRequired() : 0.0);
            case "Status" -> Comparator.comparing(p -> p.getStatus() != null ? p.getStatus().toLowerCase() : "");
            case "Date (Newest)" -> Comparator.comparing(
                    (BusinessPlan p) -> p.getLastUpdate() != null ? p.getLastUpdate()
                            : (p.getCreationDate() != null ? p.getCreationDate() : LocalDate.MIN),
                    Comparator.reverseOrder());
            case "Date (Oldest)" ->
                Comparator.comparing((BusinessPlan p) -> p.getLastUpdate() != null ? p.getLastUpdate()
                        : (p.getCreationDate() != null ? p.getCreationDate() : LocalDate.MIN));
            default -> null;
        };
        if (cmp != null) {
            allPlans.sort(cmp);
            renderCards(allPlans);
        }
    }

    private void filterCards(String query) {
        if (allPlans == null || allPlans.isEmpty()) {
            renderCards(List.of());
            return;
        }
        if (query == null || query.isBlank()) {
            renderCards(allPlans);
            return;
        }
        String q = query.toLowerCase().trim();
        renderCards(allPlans.stream().filter(p -> (p.getTitle() != null && p.getTitle().toLowerCase().contains(q)) ||
                (p.getStatus() != null && p.getStatus().toLowerCase().contains(q)) ||
                (p.getDescription() != null && p.getDescription().toLowerCase().contains(q)) ||
                (p.getMarketAnalysis() != null && p.getMarketAnalysis().toLowerCase().contains(q)))
                .collect(Collectors.toList()));
    }

    // ── Card rendering ────────────────────────────────────────

    private void renderCards(List<BusinessPlan> plans) {
        cardsContainer.getChildren().clear();
        lblCount.setText(plans.size() + " plan" + (plans.size() != 1 ? "s" : ""));

        if (plans.isEmpty()) {
            Label empty = new Label("No business plans found. Click + to create your first plan.");
            empty.getStyleClass().add("activity-meta");
            cardsContainer.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < plans.size(); i++) {
            BusinessPlan bp = plans.get(i);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/businessplancard.fxml"));
                Parent card = loader.load();
                BusinessPlanCardController ctrl = loader.getController();

                ctrl.setData(bp, this::openEditDialog, this::confirmDelete,
                        modalLayer, mainContent);

                animateIn(card, i * 55L);
                cardsContainer.getChildren().add(card);

            } catch (IOException e) {
                System.err.println("[BusinessPlanViewController] Card load error: " + e.getMessage());
            }
        }
    }

    private void animateIn(Node node, long delayMs) {
        node.setOpacity(0);
        node.setScaleX(0.88);
        node.setScaleY(0.88);
        node.setTranslateY(20);

        FadeTransition ft = new FadeTransition(Duration.millis(400), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setDelay(Duration.millis(delayMs));

        ScaleTransition st = new ScaleTransition(Duration.millis(400), node);
        st.setFromX(0.88);
        st.setToX(1.0);
        st.setFromY(0.88);
        st.setToY(1.0);
        st.setDelay(Duration.millis(delayMs));

        TranslateTransition tt = new TranslateTransition(Duration.millis(400), node);
        tt.setFromY(20);
        tt.setToY(0);
        tt.setDelay(Duration.millis(delayMs));

        new ParallelTransition(ft, st, tt).play();
    }

    // ── FAB ───────────────────────────────────────────────────

    @FXML
    private void openAddDialog() {
        openPlanDialog(null);
    }

    // ── Back navigation ───────────────────────────────────────

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/startupview.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setTitle("Startups — StartupFlow");
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            ThemeManager.getInstance().applyTo(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            showError("Cannot navigate back: " + e.getMessage());
        }
    }

    // ── Callbacks from cards ──────────────────────────────────

    private void openEditDialog(BusinessPlan bp) {
        openPlanDialog(bp);
    }

    private void confirmDelete(BusinessPlan bp) {
        String planTitle = bp.getTitle() != null ? bp.getTitle() : "this plan";
        PopupManager.showDelete(
                modalLayer, mainContent,
                "Delete Plan",
                "Delete \"" + planTitle + "\"? This cannot be undone.",
                () -> {
                    service.delete(bp);
                    loadAll();
                    AlertUtil.showSuccess("\uD83D\uDDD1  Plan Deleted",
                            "\"" + planTitle + "\" has been removed.",
                            cardsContainer.getScene().getWindow());
                });
    }

    // ── Add / Edit dialog ─────────────────────────────────────

    private void openPlanDialog(BusinessPlan existing) {
        boolean isEdit = existing != null;

        // ── Fields ──
        TextField tfTitle = styledField("Plan title…");
        TextArea taDesc = styledArea("Overall plan description…");
        TextArea taMarket = styledArea("Target market, size, trends…");
        TextArea taValueProp = styledArea("What unique value is delivered?");
        TextArea taBizModel = styledArea("Revenue streams, pricing…");
        TextArea taMarketing = styledArea("Go-to-market strategy…");
        TextArea taFinancial = styledArea("Revenue projections, burn rate…");
        TextField tfFunding = styledField("e.g. 50000");
        tfFunding.setTextFormatter(ValidationUtil.unsignedDecimalFormatter());
        TextField tfTimeline = styledField("e.g. 12 months");

        // ── Validation labels + real-time listeners ──
        Label errTitle = FormValidator.errorLabel();
        Label errFunding = FormValidator.errorLabel();
        FormValidator.clearOnType(tfTitle, errTitle);
        FormValidator.validateDoubleOnType(tfFunding, errFunding, false);

        ComboBox<String> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("Draft", "Active", "Under Review", "Pending", "Funded", "Archived");
        cbStatus.setValue("Draft");
        cbStatus.setMaxWidth(Double.MAX_VALUE);
        cbStatus.setStyle(DesignTokens.comboNormal());

        String pickerStyle = DesignTokens.comboNormal();
        DatePicker dpCreation = new DatePicker(LocalDate.now());
        DatePicker dpUpdate = new DatePicker(LocalDate.now());
        dpCreation.setMaxWidth(Double.MAX_VALUE);
        dpUpdate.setMaxWidth(Double.MAX_VALUE);
        dpCreation.setStyle(pickerStyle);
        dpUpdate.setStyle(pickerStyle);

        if (isEdit) {
            if (existing.getTitle() != null)
                tfTitle.setText(existing.getTitle());
            if (existing.getDescription() != null)
                taDesc.setText(existing.getDescription());
            if (existing.getMarketAnalysis() != null)
                taMarket.setText(existing.getMarketAnalysis());
            if (existing.getValueProposition() != null)
                taValueProp.setText(existing.getValueProposition());
            if (existing.getBusinessModel() != null)
                taBizModel.setText(existing.getBusinessModel());
            if (existing.getMarketingStrategy() != null)
                taMarketing.setText(existing.getMarketingStrategy());
            if (existing.getFinancialForecast() != null)
                taFinancial.setText(existing.getFinancialForecast());
            if (existing.getFundingRequired() != null)
                tfFunding.setText(String.valueOf(existing.getFundingRequired()));
            if (existing.getTimeline() != null)
                tfTimeline.setText(existing.getTimeline());
            if (existing.getStatus() != null)
                cbStatus.setValue(existing.getStatus());
            if (existing.getCreationDate() != null)
                dpCreation.setValue(existing.getCreationDate());
            if (existing.getLastUpdate() != null)
                dpUpdate.setValue(existing.getLastUpdate());
        }

        // ── VBox layout (single column) ──
        VBox form = new VBox(8);
        form.setPadding(new Insets(6, 4, 6, 4));
        form.getChildren().addAll(
                sectionHeader("Plan Info"),
                new VBox(4, DialogStyler.fieldLabel("Title *"), tfTitle, errTitle),
                fieldRow("Description", taDesc),
                sectionHeader("Market & Value"),
                fieldRow("Market Analysis", taMarket),
                fieldRow("Value Proposition", taValueProp),
                fieldRow("Business Model", taBizModel),
                fieldRow("Marketing Strategy", taMarketing),
                sectionHeader("Finance & Timeline"),
                fieldRow("Financial Forecast", taFinancial),
                new VBox(4, DialogStyler.fieldLabel("Funding Required (TND)"), tfFunding, errFunding),
                fieldRow("Timeline", tfTimeline),
                fieldRow("Status", cbStatus),
                fieldRow("Creation Date", dpCreation),
                fieldRow("Last Update", dpUpdate));

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(500);
        scroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Business Plan" : "New Business Plan");
        dialog.setHeaderText(isEdit ? "Edit \"" + existing.getTitle() + "\"" : "Create a New Business Plan");
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(520);
        DialogStyler.style(dialog);

        // Block dialog close if validation fails — errors shown inline
        dialog.getDialogPane().lookupButton(ButtonType.OK)
                .addEventFilter(ActionEvent.ACTION, ev -> {
                    List<String> existingTitles = allPlans.stream()
                            .map(BusinessPlan::getTitle).collect(Collectors.toList());
                    String excludeTitle = (isEdit && existing.getTitle() != null)
                            ? existing.getTitle()
                            : null;
                    Optional<String> dateOrder = (dpCreation.getValue() != null && dpUpdate.getValue() != null
                            && dpUpdate.getValue().isBefore(dpCreation.getValue()))
                                    ? Optional.of("Last Update cannot be before Creation Date.")
                                    : Optional.empty();
                    List<String> errors = ValidationUtil.gatherErrors(
                            ValidationUtil.checkName(tfTitle.getText(), "Plan Title",
                                    existingTitles, excludeTitle),
                            ValidationUtil.checkTextOptional(taDesc.getText(), "Description", 2000),
                            ValidationUtil.checkTextOptional(taMarket.getText(), "Market Analysis", 2000),
                            ValidationUtil.checkTextOptional(taValueProp.getText(), "Value Proposition", 2000),
                            ValidationUtil.checkTextOptional(taBizModel.getText(), "Business Model", 2000),
                            ValidationUtil.checkTextOptional(taMarketing.getText(), "Marketing Strategy", 2000),
                            ValidationUtil.checkTextOptional(taFinancial.getText(), "Financial Forecast", 2000),
                            ValidationUtil.checkFunding(tfFunding.getText()),
                            ValidationUtil.checkTextOptional(tfTimeline.getText(), "Timeline", 200),
                            dateOrder);
                    if (!AlertUtil.checkAndShowErrors(errors,
                            dialog.getDialogPane().getScene().getWindow())) {
                        ev.consume();
                    }
                });

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            // Validation already guaranteed by event filter
            Double funding = null;
            String fText = tfFunding.getText().trim();
            if (!fText.isBlank())
                funding = Double.parseDouble(fText);

            if (isEdit) {
                existing.setTitle(tfTitle.getText().trim());
                existing.setDescription(taDesc.getText().trim());
                existing.setMarketAnalysis(taMarket.getText().trim());
                existing.setValueProposition(taValueProp.getText().trim());
                existing.setBusinessModel(taBizModel.getText().trim());
                existing.setMarketingStrategy(taMarketing.getText().trim());
                existing.setFinancialForecast(taFinancial.getText().trim());
                existing.setFundingRequired(funding);
                existing.setTimeline(tfTimeline.getText().trim());
                existing.setStatus(cbStatus.getValue());
                existing.setCreationDate(dpCreation.getValue());
                existing.setLastUpdate(dpUpdate.getValue());
                service.update(existing);
                AlertUtil.showSuccess("\u270F  Plan Updated",
                        "\"" + existing.getTitle() + "\" has been updated successfully.",
                        cardsContainer.getScene().getWindow());
            } else {
                BusinessPlan bp = new BusinessPlan();
                bp.setTitle(tfTitle.getText().trim());
                bp.setDescription(taDesc.getText().trim());
                bp.setMarketAnalysis(taMarket.getText().trim());
                bp.setValueProposition(taValueProp.getText().trim());
                bp.setBusinessModel(taBizModel.getText().trim());
                bp.setMarketingStrategy(taMarketing.getText().trim());
                bp.setFinancialForecast(taFinancial.getText().trim());
                bp.setFundingRequired(funding);
                bp.setTimeline(tfTimeline.getText().trim());
                bp.setStatus(cbStatus.getValue());
                bp.setCreationDate(dpCreation.getValue());
                bp.setLastUpdate(dpUpdate.getValue());
                bp.setStartupID(currentStartup.getStartupID());
                if (tn.esprit.utils.SessionManager.getUser() != null) {
                    bp.setUserId(tn.esprit.utils.SessionManager.getUser().getId());
                }
                service.add(bp);
                AlertUtil.showSuccess("\u2705  Plan Created",
                        "\"" + bp.getTitle() + "\" has been added successfully.",
                        cardsContainer.getScene().getWindow());
            }
            loadAll();
        }
    }

    // ── Form builder helpers ──────────────────────────────────

    /** Label + field on dark glass card — uses light text via DialogStyler. */
    private VBox fieldRow(String labelText, Node field) {
        VBox row = new VBox(5, DialogStyler.fieldLabel(labelText), field);
        row.setStyle("-fx-background-color: transparent;");
        return row;
    }

    /** Section divider — delegates to shared DialogStyler. */
    private Label sectionHeader(String text) {
        return DialogStyler.sectionLabel(text);
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

    private void showError(String msg) {
        PopupManager.showError(modalLayer, mainContent, msg);
    }
}
