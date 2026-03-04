package tn.esprit.GUI.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.entities.BusinessPlan;
import tn.esprit.GUI.popup.DialogStyler;
import tn.esprit.GUI.popup.PopupManager;
import tn.esprit.Services.BusinessPlanService;
import tn.esprit.utils.AlertUtil;
import tn.esprit.utils.FormValidator;
import tn.esprit.utils.ValidationUtil;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * AdminBusinessPlansController — Business Plans management table page.
 *
 * Themed TableView with search/status filter, pagination and Edit/Delete actions.
 * Shares the same modal bridge pattern as the startups page.
 */
public class AdminBusinessPlansController implements Initializable {

    private static final int PAGE_SIZE = 14;

    // ── FXML ─────────────────────────────────────────────────
    @FXML private TableView<BusinessPlan>             tableBP;
    @FXML private TableColumn<BusinessPlan, Integer>  colId;
    @FXML private TableColumn<BusinessPlan, String>   colTitle;
    @FXML private TableColumn<BusinessPlan, String>   colStatus;
    @FXML private TableColumn<BusinessPlan, String>   colFunding;
    @FXML private TableColumn<BusinessPlan, String>   colStartup;
    @FXML private TableColumn<BusinessPlan, String>   colCreated;
    @FXML private TableColumn<BusinessPlan, String>   colUpdated;
    @FXML private TableColumn<BusinessPlan, Void>     colActions;

    @FXML private TextField            searchField;
    @FXML private ComboBox<String>     filterStatus;
    @FXML private Button               btnPrev;
    @FXML private Button               btnNext;
    @FXML private Label                pageInfo;
    @FXML private Label                lblTotal;

    // ── State ─────────────────────────────────────────────────
    private final BusinessPlanService  service  = new BusinessPlanService();
    private List<BusinessPlan>         allData  = new ArrayList<>();
    private List<BusinessPlan>         filtered = new ArrayList<>();
    private int                        curPage  = 0;

    // Modal bridge
    private StackPane  modalLayer;
    private Region     blurTarget;

    // ── Init ─────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        buildColumns();
        populateFilters();

        // Enable full-dataset column sorting across pages
        tableBP.setSortPolicy(tv -> {
            if (tv.getSortOrder().isEmpty()) return true;
            Comparator<BusinessPlan> cmp = null;
            for (TableColumn<BusinessPlan, ?> col : tv.getSortOrder()) {
                Comparator<BusinessPlan> colCmp = buildColumnComparator(col);
                if (colCmp == null) continue;
                cmp = (cmp == null) ? colCmp : cmp.thenComparing(colCmp);
            }
            if (cmp != null) filtered.sort(cmp);
            refreshTable();
            return true;
        });

        loadAll();
    }

    @SuppressWarnings("unchecked")
    private Comparator<BusinessPlan> buildColumnComparator(TableColumn<BusinessPlan, ?> col) {
        boolean asc = col.getSortType() == TableColumn.SortType.ASCENDING;
        Comparator<BusinessPlan> cmp = null;
        if (col == colTitle)    cmp = Comparator.comparing(p -> str(p.getTitle()).toLowerCase());
        else if (col == colStatus)  cmp = Comparator.comparing(p -> str(p.getStatus()).toLowerCase());
        else if (col == colFunding) cmp = Comparator.comparing(p -> p.getFundingRequired() != null ? p.getFundingRequired() : 0.0);
        else if (col == colStartup) cmp = Comparator.comparingInt(BusinessPlan::getStartupID);
        else if (col == colCreated) cmp = Comparator.comparing(p -> p.getCreationDate() != null ? p.getCreationDate() : LocalDate.MIN);
        else if (col == colUpdated) cmp = Comparator.comparing(p -> p.getLastUpdate() != null ? p.getLastUpdate() : LocalDate.MIN);
        else if (col == colId)      cmp = Comparator.comparingInt(BusinessPlan::getBusinessPlanID);
        if (cmp != null && !asc) cmp = cmp.reversed();
        return cmp;
    }

    public void setModalBridge(StackPane modal, Region blur) {
        this.modalLayer = modal;
        this.blurTarget = blur;
    }

    // ── Columns ──────────────────────────────────────────────

    private void buildColumns() {
        colId.setCellValueFactory(d ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        d.getValue().getBusinessPlanID()));

        colTitle.setCellValueFactory(d ->
                new SimpleStringProperty(str(d.getValue().getTitle())));

        // Status badge
        colStatus.setCellValueFactory(d ->
                new SimpleStringProperty(str(d.getValue().getStatus())));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                Label badge = new Label(s);
                badge.getStyleClass().add("status-badge-" + cssStatus(s));
                HBox wrap = new HBox(badge);
                wrap.setAlignment(Pos.CENTER_LEFT);
                setGraphic(wrap);
                setText(null);
            }
        });

        colFunding.setCellValueFactory(d -> {
            Double f = d.getValue().getFundingRequired();
            return new SimpleStringProperty(f != null
                    ? String.format("%,.0f", f) : "—");
        });

        colStartup.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getStartupID())));

        colCreated.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getCreationDate() != null
                        ? d.getValue().getCreationDate().toString() : "—"));

        colUpdated.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getLastUpdate() != null
                        ? d.getValue().getLastUpdate().toString() : "—"));

        // Actions column
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit   = new Button("✎  Edit");
            private final Button btnDelete = new Button("🗑  Delete");
            {
                btnEdit.getStyleClass().add("tbl-btn-edit");
                btnDelete.getStyleClass().add("tbl-btn-delete");
                btnEdit.setOnAction(e   -> editPlan(getTableRow().getItem()));
                btnDelete.setOnAction(e -> deletePlan(getTableRow().getItem()));
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(6, btnEdit, btnDelete);
                    box.setAlignment(Pos.CENTER_LEFT);
                    box.setPadding(new Insets(0, 4, 0, 4));
                    setGraphic(box);
                }
                setText(null);
            }
        });
    }

    // ── Filters ──────────────────────────────────────────────

    private void populateFilters() {
        filterStatus.getItems().setAll(
                "All Statuses","Draft","Active","Archived",
                "Under Review","Pending","Funded");
        filterStatus.setValue("All Statuses");
    }

    // ── Data ─────────────────────────────────────────────────

    private void loadAll() {
        allData = service.list();
        applyFilters();
    }

    private void applyFilters() {
        String query  = searchField.getText() != null
                ? searchField.getText().trim().toLowerCase() : "";
        String status = filterStatus.getValue();

        filtered = allData.stream().filter(bp -> {
            if (!query.isBlank()) {
                boolean match = contains(bp.getTitle(), query)
                        || contains(bp.getDescription(), query)
                        || contains(bp.getStatus(), query)
                        || contains(bp.getMarketAnalysis(), query)
                        || contains(bp.getValueProposition(), query);
                if (!match) return false;
            }
            if (status != null && !status.startsWith("All")
                    && !status.equalsIgnoreCase(str(bp.getStatus()))) return false;
            return true;
        }).collect(Collectors.toList());

        curPage = 0;
        refreshTable();
    }

    private void refreshTable() {
        int total = filtered.size();
        int pages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int from  = curPage * PAGE_SIZE;
        int to    = Math.min(from + PAGE_SIZE, total);
        List<BusinessPlan> page = from < total ? filtered.subList(from, to) : List.of();

        tableBP.getItems().setAll(page);
        pageInfo.setText("Page " + (curPage + 1) + " of " + pages);
        lblTotal.setText(total + " record" + (total != 1 ? "s" : ""));
        btnPrev.setDisable(curPage == 0);
        btnNext.setDisable(curPage >= pages - 1);
    }

    // ── Event handlers ────────────────────────────────────────

    @FXML private void onSearch()  { curPage = 0; applyFilters(); }
    @FXML private void onFilter()  { curPage = 0; applyFilters(); }
    @FXML private void onPrev()    { if (curPage > 0) { curPage--; refreshTable(); } }
    @FXML private void onNext()    { curPage++; refreshTable(); }

    @FXML
    private void onReset() {
        searchField.clear();
        filterStatus.setValue("All Statuses");
        curPage = 0;
        applyFilters();
    }

    // ── CRUD ─────────────────────────────────────────────────

    private void editPlan(BusinessPlan bp) {
        if (bp == null) return;

        Label      errTitle   = FormValidator.errorLabel();
        Label      errFunding = FormValidator.errorLabel();

        TextField  tfTitle    = sField(bp.getTitle(),          "Title");
        TextArea   taDesc     = sArea(bp.getDescription());
        TextArea   taMktAn    = sArea(bp.getMarketAnalysis());
        TextArea   taValProp  = sArea(bp.getValueProposition());
        TextArea   taBizMod   = sArea(bp.getBusinessModel());
        TextArea   taMktStrat = sArea(bp.getMarketingStrategy());
        TextArea   taFinFore  = sArea(bp.getFinancialForecast());
        TextField  tfFunding  = sField(bp.getFundingRequired() != null
                ? String.valueOf(bp.getFundingRequired()) : "", "Funding required ($)");
        tfFunding.setTextFormatter(ValidationUtil.unsignedDecimalFormatter());
        FormValidator.clearOnType(tfTitle, errTitle);
        FormValidator.validateDoubleOnType(tfFunding, errFunding, false);
        TextField  tfTimeline = sField(bp.getTimeline(), "Timeline");
        ComboBox<String> cbSt = styledCombo(
                "Draft","Active","Archived","Under Review","Pending","Funded");
        cbSt.setValue(bp.getStatus() != null ? bp.getStatus() : "Draft");
        TextField  tfStartup  = sField(String.valueOf(bp.getStartupID()), "Startup ID");

        VBox form = buildForm(
            DialogStyler.sectionLabel("Core Info"),
            new VBox(4, DialogStyler.fieldLabel("Title *"), tfTitle, errTitle),
            row("Description",         taDesc),
            row("Status",              cbSt),
            row("Startup ID",          tfStartup),
            DialogStyler.sectionLabel("Plan Details"),
            row("Market Analysis",     taMktAn),
            row("Value Proposition",   taValProp),
            row("Business Model",      taBizMod),
            row("Marketing Strategy",  taMktStrat),
            row("Financial Forecast",  taFinFore),
            DialogStyler.sectionLabel("Finance & Timeline"),
            new VBox(4, DialogStyler.fieldLabel("Funding Required ($)"), tfFunding, errFunding),
            row("Timeline",            tfTimeline)
        );

        Dialog<ButtonType> dlg = dialog("Edit Business Plan",
                "Edit \"" + bp.getTitle() + "\"");
        dlg.getDialogPane().setContent(wrap(form));
        DialogStyler.style(dlg);

        // Block dialog close if validation fails — errors shown inline
        dlg.getDialogPane().lookupButton(ButtonType.OK)
           .addEventFilter(ActionEvent.ACTION, ev -> {
               List<String> titles = service.list().stream()
                       .map(BusinessPlan::getTitle).collect(Collectors.toList());
               String sidText = tfStartup.getText().trim();
               Optional<String> sidErr;
               try {
                   int parsed = Integer.parseInt(sidText);
                   sidErr = parsed <= 0
                       ? Optional.of("Startup ID must be a positive integer.")
                       : Optional.empty();
               } catch (NumberFormatException ex) {
                   sidErr = sidText.isEmpty()
                       ? Optional.of("Startup ID is required.")
                       : Optional.of("Startup ID must be a valid integer.");
               }
               List<String> errors = ValidationUtil.gatherErrors(
                   ValidationUtil.checkName(tfTitle.getText(), "Plan Title", titles, bp.getTitle()),
                   ValidationUtil.checkTextOptional(taDesc.getText(),    "Description",        2000),
                   ValidationUtil.checkTextOptional(taMktAn.getText(),   "Market Analysis",    2000),
                   ValidationUtil.checkTextOptional(taValProp.getText(), "Value Proposition",  2000),
                   ValidationUtil.checkTextOptional(taBizMod.getText(),  "Business Model",     2000),
                   ValidationUtil.checkTextOptional(taMktStrat.getText(),"Marketing Strategy", 2000),
                   ValidationUtil.checkTextOptional(taFinFore.getText(), "Financial Forecast", 2000),
                   ValidationUtil.checkFunding(tfFunding.getText()),
                   ValidationUtil.checkTextOptional(tfTimeline.getText(),"Timeline",           200),
                   sidErr
               );
               if (!AlertUtil.checkAndShowErrors(errors,
                       dlg.getDialogPane().getScene().getWindow())) {
                   ev.consume();
               }
           });

        dlg.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            // Validation already guaranteed by event filter
            String title = tfTitle.getText().trim();
            bp.setTitle(title);
            bp.setDescription(taDesc.getText().trim());
            bp.setMarketAnalysis(taMktAn.getText().trim());
            bp.setValueProposition(taValProp.getText().trim());
            bp.setBusinessModel(taBizMod.getText().trim());
            bp.setMarketingStrategy(taMktStrat.getText().trim());
            bp.setFinancialForecast(taFinFore.getText().trim());
            bp.setTimeline(tfTimeline.getText().trim());
            bp.setStatus(cbSt.getValue());
            bp.setLastUpdate(LocalDate.now());
            try {
                bp.setStartupID(Integer.parseInt(tfStartup.getText().trim()));
            } catch (NumberFormatException ignored) {}
            try {
                bp.setFundingRequired(tfFunding.getText().isBlank()
                        ? null : Double.parseDouble(tfFunding.getText().trim()));
            } catch (NumberFormatException ignored) {}
            service.update(bp);
            loadAll();
            AlertUtil.showSuccess("\u270F  Plan Updated",
                "Business plan \"" + bp.getTitle() + "\" has been updated.",
                tableBP.getScene().getWindow());
        });
    }

    private void deletePlan(BusinessPlan bp) {
        if (bp == null || modalLayer == null) return;
        PopupManager.showDelete(modalLayer, blurTarget,
                "Delete Business Plan",
                "Delete \"" + bp.getTitle() + "\"? This action cannot be undone.",
                () -> {
                    service.delete(bp);
                    loadAll();
                    AlertUtil.showSuccess("\uD83D\uDDD1  Plan Deleted",
                        "\"" + bp.getTitle() + "\" has been removed.",
                        tableBP.getScene().getWindow());
                });
    }

    // ── Form helpers ─────────────────────────────────────────

    private TextField sField(String val, String prompt) {
        TextField tf = new TextField(val != null ? val : "");
        tf.setPromptText(prompt);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle(DialogStyler.inputStyle());
        return tf;
    }
    private TextArea sArea(String val) {
        TextArea ta = new TextArea(val != null ? val : "");
        ta.setPrefRowCount(3);
        ta.setWrapText(true);
        ta.setMaxWidth(Double.MAX_VALUE);
        ta.setStyle(DialogStyler.inputStyle());
        return ta;
    }
    private ComboBox<String> styledCombo(String... items) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(items);
        cb.setValue(items[0]);
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setStyle("-fx-background-color:rgba(255,255,255,0.90);" +
                    "-fx-border-color:rgba(167,139,250,0.55);" +
                    "-fx-border-radius:12;-fx-background-radius:12;" +
                    "-fx-border-width:1.5;-fx-font-size:13px;-fx-padding:3 0 3 6;");
        return cb;
    }
    private VBox row(String label, Node field) {
        return new VBox(5, DialogStyler.fieldLabel(label), field);
    }
    private VBox buildForm(Node... nodes) {
        VBox form = new VBox(8, nodes);
        form.setPadding(new Insets(6, 4, 6, 4));
        return form;
    }
    private ScrollPane wrap(VBox form) {
        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true);
        sp.setPrefHeight(500);
        sp.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");
        return sp;
    }
    private Dialog<ButtonType> dialog(String title, String header) {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle(title);
        d.setHeaderText(header);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.getDialogPane().setPrefWidth(540);
        return d;
    }

    // ── Misc ─────────────────────────────────────────────────

    private static String str(String s) { return s != null ? s : "—"; }
    private static boolean contains(String f, String q) {
        return f != null && f.toLowerCase().contains(q);
    }
    private static String cssStatus(String s) {
        if (s == null) return "default";
        return switch (s.toLowerCase()) {
            case "active"       -> "active";
            case "inactive"     -> "inactive";
            case "under review" -> "review";
            case "approved"     -> "approved";
            case "funded"       -> "approved";
            case "scaling"      -> "scaling";
            default             -> "default";
        };
    }
}

