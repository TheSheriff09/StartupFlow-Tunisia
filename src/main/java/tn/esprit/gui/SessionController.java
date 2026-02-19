package tn.esprit.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entities.Session;
import tn.esprit.services.SessionService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SessionController implements Initializable {

    @FXML private TextField  mentorIDField;
    @FXML private TextField  entrepreneurIDField;
    @FXML private TextField  startupIDField;
    @FXML private DatePicker sessionDatePicker;
    @FXML private ComboBox<String> sessionTypeCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField  scheduleIDField;
    @FXML private TextArea   notesArea;
    @FXML private Label      msgLabel;
    @FXML private TextField  searchField;

    @FXML private TableView<Session>                  sessionTable;
    @FXML private TableColumn<Session, Integer>       colID;
    @FXML private TableColumn<Session, Integer>       colMentor;
    @FXML private TableColumn<Session, Integer>       colEntrepreneur;
    @FXML private TableColumn<Session, Integer>       colStartup;
    @FXML private TableColumn<Session, String>        colDate;
    @FXML private TableColumn<Session, String>        colType;
    @FXML private TableColumn<Session, String>        colStatus;
    @FXML private TableColumn<Session, String>        colNotes;

    private final SessionService service = new SessionService();
    private ObservableList<Session> allData;
    private Session selected;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sessionTypeCombo.setItems(FXCollections.observableArrayList("online", "onsite"));
        statusCombo.setItems(FXCollections.observableArrayList("planned", "completed", "cancelled"));

        colID.setCellValueFactory(new PropertyValueFactory<>("sessionID"));
        colMentor.setCellValueFactory(new PropertyValueFactory<>("mentorID"));
        colEntrepreneur.setCellValueFactory(new PropertyValueFactory<>("entrepreneurID"));
        colStartup.setCellValueFactory(new PropertyValueFactory<>("startupID"));
        colType.setCellValueFactory(new PropertyValueFactory<>("sessionType"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getSessionDate() != null ? cd.getValue().getSessionDate().toString() : ""));
        colStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus()));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item.toLowerCase()) {
                    case "planned"   -> setStyle("-fx-text-fill:#7357ff;-fx-font-weight:700;");
                    case "completed" -> setStyle("-fx-text-fill:#12a059;-fx-font-weight:700;");
                    case "cancelled" -> setStyle("-fx-text-fill:#e0134a;-fx-font-weight:700;");
                    default -> setStyle("");
                }
            }
        });
        loadTable();
    }

    @FXML private void onAdd() {
        clearMsg();
        Session s = buildFromForm();
        if (s == null) return;
        try { service.add(s); showSuccess("Session added!"); onClear(); loadTable(); }
        catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML private void onUpdate() {
        clearMsg();
        if (selected == null) { showError("Select a row to update."); return; }
        Session s = buildFromForm();
        if (s == null) return;
        s.setSessionID(selected.getSessionID());
        try { service.update(s); showSuccess("Session updated!"); onClear(); loadTable(); }
        catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML private void onDelete() {
        clearMsg();
        if (selected == null) { showError("Select a row to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete session #" + selected.getSessionID() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try { service.delete(selected); showSuccess("Session deleted!"); onClear(); loadTable(); }
                catch (Exception e) { showError(e.getMessage()); }
            }
        });
    }

    @FXML private void onClear() {
        mentorIDField.clear(); entrepreneurIDField.clear(); startupIDField.clear();
        sessionDatePicker.setValue(null); sessionTypeCombo.setValue(null);
        statusCombo.setValue(null); scheduleIDField.clear(); notesArea.clear();
        clearMsg(); selected = null;
        sessionTable.getSelectionModel().clearSelection();
    }

    @FXML private void onRowSelected() {
        Session s = sessionTable.getSelectionModel().getSelectedItem();
        if (s == null) return;
        selected = s;
        mentorIDField.setText(String.valueOf(s.getMentorID()));
        entrepreneurIDField.setText(String.valueOf(s.getEntrepreneurID()));
        startupIDField.setText(String.valueOf(s.getStartupID()));
        sessionDatePicker.setValue(s.getSessionDate());
        sessionTypeCombo.setValue(s.getSessionType());
        statusCombo.setValue(s.getStatus());
        scheduleIDField.setText(s.getScheduleID() > 0 ? String.valueOf(s.getScheduleID()) : "");
        notesArea.setText(s.getNotes());
    }

    @FXML private void onSearch() {
        String q = searchField.getText().toLowerCase();
        if (q.isBlank()) { sessionTable.setItems(allData); return; }
        List<Session> f = allData.stream()
                .filter(s -> (s.getStatus() != null && s.getStatus().toLowerCase().contains(q))
                        || (s.getSessionType() != null && s.getSessionType().toLowerCase().contains(q)))
                .collect(Collectors.toList());
        sessionTable.setItems(FXCollections.observableArrayList(f));
    }

    private Session buildFromForm() {
        int mentorID, entID, startupID;
        try { mentorID = Integer.parseInt(mentorIDField.getText().trim()); }
        catch (NumberFormatException e) { showError("Mentor ID must be a number."); return null; }
        try { entID = Integer.parseInt(entrepreneurIDField.getText().trim()); }
        catch (NumberFormatException e) { showError("Entrepreneur ID must be a number."); return null; }
        try { startupID = Integer.parseInt(startupIDField.getText().trim()); }
        catch (NumberFormatException e) { showError("Startup ID must be a number (use 1 if unsure)."); return null; }
        if (sessionDatePicker.getValue() == null) { showError("Session date is required."); return null; }
        if (sessionTypeCombo.getValue() == null)  { showError("Session type is required."); return null; }
        if (statusCombo.getValue() == null)        { showError("Status is required."); return null; }
        int schedID = 0;
        String sch = scheduleIDField.getText().trim();
        if (!sch.isEmpty()) {
            try { schedID = Integer.parseInt(sch); }
            catch (NumberFormatException e) { showError("Schedule ID must be a number."); return null; }
        }
        Session s = new Session(mentorID, entID, startupID,
                sessionDatePicker.getValue(), sessionTypeCombo.getValue(), notesArea.getText());
        s.setStatus(statusCombo.getValue());
        s.setScheduleID(schedID);
        return s;
    }

    private void loadTable() {
        allData = FXCollections.observableArrayList(service.list());
        sessionTable.setItems(allData);
    }

    private void showError(String msg)   { msgLabel.setText("⚠ " + msg); msgLabel.setStyle("-fx-text-fill:#e0134a;-fx-font-weight:700;"); }
    private void showSuccess(String msg) { msgLabel.setText("✓ " + msg); msgLabel.setStyle("-fx-text-fill:#12a059;-fx-font-weight:700;"); }
    private void clearMsg()              { msgLabel.setText(""); }
}
