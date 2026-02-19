package tn.esprit.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entities.Schedule;
import tn.esprit.services.ScheduleService;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ScheduleController implements Initializable {

    @FXML private TextField  mentorIDField;
    @FXML private DatePicker datePicker;
    @FXML private TextField  startTimeField;
    @FXML private TextField  endTimeField;
    @FXML private Label      msgLabel;
    @FXML private TextField  searchField;

    @FXML private TableView<Schedule>            scheduleTable;
    @FXML private TableColumn<Schedule, Integer> colID;
    @FXML private TableColumn<Schedule, Integer> colMentor;
    @FXML private TableColumn<Schedule, String>  colDate;
    @FXML private TableColumn<Schedule, String>  colStart;
    @FXML private TableColumn<Schedule, String>  colEnd;
    @FXML private TableColumn<Schedule, String>  colBooked;

    private final ScheduleService service = new ScheduleService();
    private ObservableList<Schedule> allData;
    private Schedule selected;
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colID.setCellValueFactory(new PropertyValueFactory<>("scheduleID"));
        colMentor.setCellValueFactory(new PropertyValueFactory<>("mentorID"));
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getAvailableDate() != null ? cd.getValue().getAvailableDate().toString() : ""));
        colStart.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getStartTime() != null ? cd.getValue().getStartTime().format(TF) : ""));
        colEnd.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getEndTime() != null ? cd.getValue().getEndTime().format(TF) : ""));
        colBooked.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().isBooked() ? "✅ Yes" : "⬜ No"));
        loadTable();
    }

    @FXML private void onAdd() {
        clearMsg();
        Schedule s = buildFromForm();
        if (s == null) return;
        try { service.add(s); showSuccess("Slot added!"); onClear(); loadTable(); }
        catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML private void onUpdate() {
        clearMsg();
        if (selected == null) { showError("Select a row to update."); return; }
        Schedule s = buildFromForm();
        if (s == null) return;
        s.setScheduleID(selected.getScheduleID());
        s.setBooked(selected.isBooked());
        try { service.update(s); showSuccess("Slot updated!"); onClear(); loadTable(); }
        catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML private void onDelete() {
        clearMsg();
        if (selected == null) { showError("Select a row to delete."); return; }
        try { service.delete(selected); showSuccess("Slot deleted!"); onClear(); loadTable(); }
        catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML private void onClear() {
        mentorIDField.clear(); datePicker.setValue(null);
        startTimeField.clear(); endTimeField.clear();
        clearMsg(); selected = null;
        scheduleTable.getSelectionModel().clearSelection();
    }

    @FXML private void onRowSelected() {
        Schedule s = scheduleTable.getSelectionModel().getSelectedItem();
        if (s == null) return;
        selected = s;
        mentorIDField.setText(String.valueOf(s.getMentorID()));
        datePicker.setValue(s.getAvailableDate());
        startTimeField.setText(s.getStartTime() != null ? s.getStartTime().format(TF) : "");
        endTimeField.setText(s.getEndTime() != null ? s.getEndTime().format(TF) : "");
    }

    @FXML private void onSearch() {
        String q = searchField.getText().toLowerCase();
        if (q.isBlank()) { scheduleTable.setItems(allData); return; }
        List<Schedule> f = allData.stream()
                .filter(s -> String.valueOf(s.getMentorID()).contains(q)
                        || (s.getAvailableDate() != null && s.getAvailableDate().toString().contains(q)))
                .collect(Collectors.toList());
        scheduleTable.setItems(FXCollections.observableArrayList(f));
    }

    private Schedule buildFromForm() {
        int mentorID;
        try { mentorID = Integer.parseInt(mentorIDField.getText().trim()); }
        catch (NumberFormatException e) { showError("Mentor ID must be a number."); return null; }
        LocalDate date = datePicker.getValue();
        if (date == null) { showError("Please select a date."); return null; }
        LocalTime start, end;
        try { start = LocalTime.parse(startTimeField.getText().trim(), TF); }
        catch (DateTimeParseException e) { showError("Start time must be HH:mm (e.g. 09:00)."); return null; }
        try { end = LocalTime.parse(endTimeField.getText().trim(), TF); }
        catch (DateTimeParseException e) { showError("End time must be HH:mm (e.g. 10:00)."); return null; }
        return new Schedule(mentorID, date, start, end);
    }

    private void loadTable() {
        allData = FXCollections.observableArrayList(service.list());
        scheduleTable.setItems(allData);
    }

    private void showError(String msg)   { msgLabel.setText("⚠ " + msg); msgLabel.setStyle("-fx-text-fill:#e0134a;-fx-font-weight:700;"); }
    private void showSuccess(String msg) { msgLabel.setText("✓ " + msg); msgLabel.setStyle("-fx-text-fill:#12a059;-fx-font-weight:700;"); }
    private void clearMsg()              { msgLabel.setText(""); }
}
