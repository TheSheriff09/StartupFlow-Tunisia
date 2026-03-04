package tn.esprit.GUI.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import tn.esprit.entities.Startup;
import tn.esprit.GUI.popup.DialogStyler;
import tn.esprit.GUI.popup.PopupManager;
import tn.esprit.GUI.startup.AddStartupController;
import tn.esprit.Services.StartupService;
import tn.esprit.utils.AlertUtil;
import tn.esprit.utils.FormValidator;
import tn.esprit.utils.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AdminStartupsController — Startups Management table page.
 *
 * Full CRUD: themed TableView with search/filter, pagination,
 * action buttons per row (Edit / Delete), and an Add FAB.
 * Uses the same modal layer + PopupManager as the main app.
 */
public class AdminStartupsController implements Initializable {

    private static final int PAGE_SIZE = 14;

    // ── FXML ─────────────────────────────────────────────────
    @FXML private TableView<Startup>   tableStartups;
    @FXML private TableColumn<Startup, Integer> colId;
    @FXML private TableColumn<Startup, String>  colName;
    @FXML private TableColumn<Startup, String>  colSector;
    @FXML private TableColumn<Startup, String>  colStage;
    @FXML private TableColumn<Startup, String>  colStatus;
    @FXML private TableColumn<Startup, String>  colKpi;
    @FXML private TableColumn<Startup, String>  colFunding;
    @FXML private TableColumn<Startup, Void>    colActions;

    @FXML private TextField            searchField;
    @FXML private ComboBox<String>     filterSector;
    @FXML private ComboBox<String>     filterStage;
    @FXML private ComboBox<String>     filterStatus;

    @FXML private Button               btnPrev;
    @FXML private Button               btnNext;
    @FXML private Label                pageInfo;
    @FXML private Label                lblTotal;

    // ── State ─────────────────────────────────────────────────
    private final StartupService      service    = new StartupService();
    private List<Startup>             allData    = new ArrayList<>();
    private List<Startup>             filtered   = new ArrayList<>();
    private int                       curPage    = 0;

    // Modal bridge (injected by AdminDashboardController)
    private StackPane  modalLayer;
    private Region     blurTarget;

    // ── Init ─────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        buildColumns();
        populateFilters();

        // Enable full-dataset column sorting (not just current page)
        tableStartups.setSortPolicy(tv -> {
            if (tv.getSortOrder().isEmpty()) return true;
            Comparator<Startup> cmp = null;
            for (TableColumn<Startup, ?> col : tv.getSortOrder()) {
                Comparator<Startup> colCmp = buildColumnComparator(col);
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
    private Comparator<Startup> buildColumnComparator(TableColumn<Startup, ?> col) {
        boolean asc = col.getSortType() == TableColumn.SortType.ASCENDING;
        Comparator<Startup> cmp = null;
        if (col == colName)    cmp = Comparator.comparing(s -> str(s.getName()).toLowerCase());
        else if (col == colSector) cmp = Comparator.comparing(s -> str(s.getSector()).toLowerCase());
        else if (col == colStage)  cmp = Comparator.comparing(s -> str(s.getStage()).toLowerCase());
        else if (col == colStatus) cmp = Comparator.comparing(s -> str(s.getStatus()).toLowerCase());
        else if (col == colFunding) cmp = Comparator.comparing(s -> s.getFundingAmount() != null ? s.getFundingAmount() : 0.0);
        else if (col == colKpi)    cmp = Comparator.comparing(s -> s.getKpiScore() != null ? s.getKpiScore() : 0.0);
        else if (col == colId)     cmp = Comparator.comparingInt(Startup::getStartupID);
        if (cmp != null && !asc) cmp = cmp.reversed();
        return cmp;
    }

    /** Called by the dashboard after loading this page. */
    public void setModalBridge(StackPane modal, Region blur) {
        this.modalLayer = modal;
        this.blurTarget = blur;
    }

    // ── Column setup ─────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void buildColumns() {
        colId.setCellValueFactory(d ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        d.getValue().getStartupID()));

        colName.setCellValueFactory(d ->
                new SimpleStringProperty(str(d.getValue().getName())));

        colSector.setCellValueFactory(d ->
                new SimpleStringProperty(str(d.getValue().getSector())));

        colStage.setCellValueFactory(d ->
                new SimpleStringProperty(str(d.getValue().getStage())));

        // Status column — colored badge label
        colStatus.setCellValueFactory(d ->
                new SimpleStringProperty(str(d.getValue().getStatus())));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                Label badge = new Label(s);
                badge.getStyleClass().addAll("status-badge-" + cssStatus(s));
                HBox wrap = new HBox(badge);
                wrap.setAlignment(Pos.CENTER_LEFT);
                setGraphic(wrap);
                setText(null);
            }
        });

        colKpi.setCellValueFactory(d -> {
            Double kpi = d.getValue().getKpiScore();
            return new SimpleStringProperty(kpi != null
                    ? String.format("%.1f", kpi) : "—");
        });

        colFunding.setCellValueFactory(d -> {
            Double f = d.getValue().getFundingAmount();
            return new SimpleStringProperty(f != null
                    ? String.format("%,.0f", f) : "—");
        });

        // Actions column — Edit + Delete buttons
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit   = new Button("✎  Edit");
            private final Button btnDelete = new Button("🗑  Delete");
            {
                btnEdit.getStyleClass().add("tbl-btn-edit");
                btnDelete.getStyleClass().add("tbl-btn-delete");
                btnEdit.setTooltip(new javafx.scene.control.Tooltip("Edit this startup"));
                btnDelete.setTooltip(new javafx.scene.control.Tooltip("Delete this startup"));
                btnEdit.setOnAction(e  -> editStartup(getTableRow().getItem()));
                btnDelete.setOnAction(e -> deleteStartup(getTableRow().getItem()));
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

    // ── Filter combo population ──────────────────────────────

    private void populateFilters() {
        List<String> sectors = new ArrayList<>();
        sectors.add("All Sectors");
        sectors.addAll(List.of("FinTech","HealthTech","EdTech","AgriTech",
                "E-Commerce","CleanTech","B2B SaaS","Other"));
        filterSector.getItems().setAll(sectors);
        filterSector.setValue("All Sectors");

        List<String> stages = new ArrayList<>();
        stages.add("All Stages");
        stages.addAll(List.of("Idea","MVP","Seed","Growth","Mature","Scaling"));
        filterStage.getItems().setAll(stages);
        filterStage.setValue("All Stages");

        List<String> statuses = new ArrayList<>();
        statuses.add("All Statuses");
        statuses.addAll(List.of("Active","Inactive","Under Review","Approved","Scaling"));
        filterStatus.getItems().setAll(statuses);
        filterStatus.setValue("All Statuses");
    }

    // ── Data loading ─────────────────────────────────────────

    private void loadAll() {
        allData = service.list();
        applyFilters();
    }

    private void applyFilters() {
        String query  = searchField.getText()  != null ? searchField.getText().trim().toLowerCase() : "";
        String sector = filterSector.getValue();
        String stage  = filterStage.getValue();
        String status = filterStatus.getValue();

        filtered = allData.stream().filter(s -> {
            if (!query.isBlank()) {
                boolean match = contains(s.getName(), query)
                        || contains(s.getSector(), query)
                        || contains(s.getStage(), query)
                        || contains(s.getStatus(), query)
                        || contains(s.getDescription(), query);
                if (!match) return false;
            }
            if (sector != null && !sector.startsWith("All")
                    && !sector.equalsIgnoreCase(str(s.getSector()))) return false;
            if (stage  != null && !stage.startsWith("All")
                    && !stage.equalsIgnoreCase(str(s.getStage())))   return false;
            if (status != null && !status.startsWith("All")
                    && !status.equalsIgnoreCase(str(s.getStatus()))) return false;
            return true;
        }).collect(Collectors.toList());

        curPage = 0;
        refreshTable();
    }

    private void refreshTable() {
        int total = filtered.size();
        int pages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));

        int from = curPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);
        List<Startup> page = from < total ? filtered.subList(from, to) : List.of();

        tableStartups.getItems().setAll(page);
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
        filterSector.setValue("All Sectors");
        filterStage.setValue("All Stages");
        filterStatus.setValue("All Statuses");
        curPage = 0;
        applyFilters();
    }

    // ── FAB — open Add Startup overlay ───────────────────────

    @FXML
    private void openAddDialog() {
        if (modalLayer == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/addstartupdialog.fxml"));
            Node overlay = loader.load();
            AddStartupController ctrl = loader.getController();

            ctrl.setOnSave(s -> {
                service.add(s);
                loadAll();
                AlertUtil.showSuccess("\u2705  Startup Created",
                    "\"" + s.getName() + "\" has been added successfully.",
                    tableStartups.getScene().getWindow());
            });
            ctrl.setOnClose(this::closeModal);

            modalLayer.getChildren().setAll(overlay);
            modalLayer.setVisible(true);
            modalLayer.setManaged(true);
            if (blurTarget != null) blurTarget.setEffect(new GaussianBlur(7));
            ctrl.animateOpen();
        } catch (IOException e) {
            System.err.println("[AdminStartupsController.openAddDialog] " + e.getMessage());
        }
    }

    // ── CRUD actions ─────────────────────────────────────────

    private void editStartup(Startup s) {
        if (s == null) return;

        TextField        tfName      = styledField(s.getName(), "Name");
        TextField        tfSector    = styledField(s.getSector(), "Sector");
        TextArea         taDesc      = styledArea(s.getDescription());
        DatePicker       dpCreation  = styledDatePicker(s.getCreationDate());
        TextField        tfStage     = styledField(s.getStage(), "Stage");
        ComboBox<String> cbStatus    = styledCombo(
            "Active","Inactive","Under Review","Approved","Scaling");
        cbStatus.setValue(s.getStatus() != null ? s.getStatus() : "Active");
        TextField        tfFunding   = styledField(
            s.getFundingAmount() != null ? String.valueOf(s.getFundingAmount()) : "",
            "Funding amount");
        tfFunding.setTextFormatter(ValidationUtil.unsignedDecimalFormatter());
        TextField        tfIncubator = styledField(s.getIncubatorProgram(), "Incubator program");

        // ── Validation labels ──
        Label errName    = FormValidator.errorLabel();
        Label errFunding = FormValidator.errorLabel();
        Label errImage   = FormValidator.errorLabel();
        FormValidator.clearOnType(tfName, errName);
        FormValidator.enforceLengthLimit(tfName, 100);
        FormValidator.validateDoubleOnType(tfFunding, errFunding, false);

        // ── Image picker ──
        String[] imageHolder = { s.getImageURL() };
        HBox     imageRow    = buildImagePicker(imageHolder, errImage, s.getImageURL());

        VBox form = buildForm(
            DialogStyler.sectionLabel("Basic Info"),
            FormValidator.fieldRow("Name *",        tfName,      errName),
            FormValidator.fieldRow("Sector",         tfSector,    null),
            FormValidator.fieldRow("Description",    taDesc,      null),
            new VBox(5, DialogStyler.fieldLabel("Image"), imageRow, errImage),
            DialogStyler.sectionLabel("Details"),
            FormValidator.fieldRow("Creation Date",  dpCreation,  null),
            FormValidator.fieldRow("Stage",           tfStage,     null),
            FormValidator.fieldRow("Status",          cbStatus,    null),
            DialogStyler.sectionLabel("Finance"),
            FormValidator.fieldRow("Funding (USD)",  tfFunding,   errFunding),
            FormValidator.fieldRow("Incubator",       tfIncubator, null)
        );

        Dialog<ButtonType> dlg = dialog("Edit Startup",
                "Edit \"" + s.getName() + "\"");
        dlg.getDialogPane().setContent(wrap(form));
        DialogStyler.style(dlg);

        // Block dialog close if validation fails — errors shown inline
        dlg.getDialogPane().lookupButton(ButtonType.OK)
           .addEventFilter(ActionEvent.ACTION, ev -> {
               // Collect strict validation errors
               List<String> errors = new ArrayList<>();

               // Existing names, excluding the current startup being edited
               List<String> existingNames = allData.stream()
                       .map(Startup::getName)
                       .collect(Collectors.toList());

               ValidationUtil.collect(
                   ValidationUtil.checkName(tfName.getText(), "Name", existingNames,
                           s.getName()),
                   errors);
               ValidationUtil.collect(
                   ValidationUtil.checkFunding(tfFunding.getText()),
                   errors);
               boolean imageOk = FormValidator.requireImageExtension(imageHolder[0], errImage);
               if (!imageOk) errors.add("Image file must be PNG, JPG, or JPEG.");

               // Apply red borders on failing fields
               ValidationUtil.checkName(tfName.getText(), "Name", existingNames, s.getName())
                   .ifPresentOrElse(
                       msg -> ValidationUtil.markFieldError(tfName, errName, msg),
                       ()  -> ValidationUtil.clearFieldError(tfName, errName));
               ValidationUtil.checkFunding(tfFunding.getText())
                   .ifPresentOrElse(
                       msg -> ValidationUtil.markFieldError(tfFunding, errFunding, msg),
                       ()  -> ValidationUtil.clearFieldError(tfFunding, errFunding));

               if (!errors.isEmpty()) {
                   AlertUtil.showValidationErrors(errors,
                       dlg.getDialogPane().getScene().getWindow());
                   ev.consume();
               }
           });

        dlg.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            // Validation already guaranteed by event filter
            s.setName(tfName.getText().trim());
            s.setSector(tfSector.getText().trim());
            s.setDescription(taDesc.getText().trim());
            s.setImageURL(imageHolder[0]);
            s.setCreationDate(dpCreation.getValue());
            s.setStage(tfStage.getText().trim());
            s.setStatus(cbStatus.getValue());
            try { s.setFundingAmount(tfFunding.getText().isBlank()
                    ? null : Double.parseDouble(tfFunding.getText().trim()));
            } catch (NumberFormatException ignored) {}
            s.setIncubatorProgram(tfIncubator.getText().trim());
            service.update(s);
            loadAll();
            AlertUtil.showSuccess("\u270F  Startup Updated",
                "\"" + s.getName() + "\" has been updated successfully.",
                tableStartups.getScene().getWindow());
        });
    }

    private void deleteStartup(Startup s) {
        if (s == null || modalLayer == null) return;
        PopupManager.showDelete(modalLayer, blurTarget,
                "Delete Startup",
                "Delete \"" + s.getName() + "\"? All linked business plans will also be removed.",
                () -> {
                    service.delete(s);
                    loadAll();
                    AlertUtil.showSuccess("\uD83D\uDDD1  Startup Deleted",
                        "\"" + s.getName() + "\" has been removed.",
                        tableStartups.getScene().getWindow());
                });
    }

    // ── Modal helpers ────────────────────────────────────────

    private void closeModal() {
        if (modalLayer == null) return;
        modalLayer.getChildren().clear();
        modalLayer.setVisible(false);
        modalLayer.setManaged(false);
        if (blurTarget != null) blurTarget.setEffect(null);
    }

    // ── Form builder helpers ─────────────────────────────────

    private TextField styledField(String val, String prompt) {
        TextField tf = new TextField(val != null ? val : "");
        tf.setPromptText(prompt);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle(DialogStyler.inputStyle());
        return tf;
    }
    private TextArea styledArea(String val) {
        TextArea ta = new TextArea(val != null ? val : "");
        ta.setPromptText("Description…");
        ta.setPrefRowCount(3);
        ta.setWrapText(true);
        ta.setMaxWidth(Double.MAX_VALUE);
        ta.setStyle(DialogStyler.inputStyle());
        return ta;
    }
    private DatePicker styledDatePicker(LocalDate val) {
        DatePicker dp = new DatePicker(val != null ? val : LocalDate.now());
        dp.setMaxWidth(Double.MAX_VALUE);
        dp.setStyle("-fx-background-color:rgba(255,255,255,0.90);" +
                    "-fx-border-color:rgba(167,139,250,0.55);" +
                    "-fx-border-radius:12;-fx-background-radius:12;" +
                    "-fx-border-width:1.5;-fx-font-size:13px;-fx-padding:3 0 3 6;");
        return dp;
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
        sp.setPrefHeight(440);
        sp.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");
        return sp;
    }
    private Dialog<ButtonType> dialog(String title, String header) {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle(title);
        d.setHeaderText(header);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.getDialogPane().setPrefWidth(500);
        return d;
    }

    // ── Image picker helper ──────────────────────────────────

    private HBox buildImagePicker(String[] holder, Label errLbl, String existingPath) {
        ImageView preview = new ImageView();
        preview.setFitWidth(72); preview.setFitHeight(72);
        preview.setPreserveRatio(true);

        Label placeholder = new Label("📷");
        placeholder.setStyle("-fx-font-size:26px;-fx-text-fill:rgba(196,181,253,0.82);");

        StackPane thumb = new StackPane(preview, placeholder);
        thumb.setPrefSize(84, 84); thumb.setMinSize(84, 84); thumb.setMaxSize(84, 84);
        thumb.setStyle(
            "-fx-background-color:rgba(237,233,254,0.50);" +
            "-fx-border-color:rgba(167,139,250,0.72);" +
            "-fx-border-radius:14;-fx-background-radius:14;-fx-border-width:1.5;");

        if (existingPath != null && !existingPath.isBlank()) {
            try {
                File f = new File(existingPath);
                if (f.exists()) { preview.setImage(new Image(f.toURI().toString())); placeholder.setVisible(false); }
            } catch (Exception ignored) {}
        }

        Label pathLabel = new Label(existingPath != null && !existingPath.isBlank()
                ? new File(existingPath).getName() : "No image selected");
        pathLabel.setStyle("-fx-font-size:10.5px;-fx-text-fill:rgba(196,181,253,0.92);-fx-font-style:italic;");
        pathLabel.setWrapText(true);

        Button btnChoose = new Button("📁  Change Image");
        btnChoose.setStyle(
            "-fx-background-color:rgba(237,233,254,0.86);" +
            "-fx-border-color:rgba(167,139,250,0.72);" +
            "-fx-border-radius:12;-fx-background-radius:12;" +
            "-fx-border-width:1.5;-fx-text-fill:#6d28d9;" +
            "-fx-font-size:13px;-fx-font-weight:800;" +
            "-fx-padding:9 18 9 18;-fx-cursor:hand;");
        btnChoose.setMaxWidth(Double.MAX_VALUE);
        btnChoose.setOnAction(e -> {
            FileChooser fc = new FileChooser();
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

    // ── Misc helpers ─────────────────────────────────────────

    private static String str(String s) { return s != null ? s : "—"; }
    private static boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }
    /** Map status string to CSS suffix */
    private static String cssStatus(String s) {
        if (s == null) return "default";
        return switch (s.toLowerCase()) {
            case "active"       -> "active";
            case "inactive"     -> "inactive";
            case "under review" -> "review";
            case "approved"     -> "approved";
            case "scaling"      -> "scaling";
            default             -> "default";
        };
    }
}

