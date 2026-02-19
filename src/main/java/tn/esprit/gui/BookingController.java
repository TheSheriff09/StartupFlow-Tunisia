package tn.esprit.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entities.Booking;
import tn.esprit.services.BookingService;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BookingController implements Initializable {

    @FXML private TextField  entrepreneurIDField;
    @FXML private TextField  mentorIDField;
    @FXML private TextField  startupIDField;
    @FXML private DatePicker requestedDatePicker;
    @FXML private TextField  requestedTimeField;
    @FXML private TextField  topicField;
    @FXML private Label      msgLabel;
    @FXML private TextField  searchField;

    @FXML private TableView<Booking>                   bookingTable;
    @FXML private TableColumn<Booking, Integer>        colID;
    @FXML private TableColumn<Booking, Integer>        colEntrepreneur;
    @FXML private TableColumn<Booking, Integer>        colMentor;
    @FXML private TableColumn<Booking, Integer>        colStartup;
    @FXML private TableColumn<Booking, String>         colDate;
    @FXML private TableColumn<Booking, String>         colTime;
    @FXML private TableColumn<Booking, String>         colTopic;
    @FXML private TableColumn<Booking, String>         colStatus;
    @FXML private TableColumn<Booking, String>         colCreated;

    private final BookingService service = new BookingService();
    private ObservableList<Booking> allData;
    private Booking selected;
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colID.setCellValueFactory(new PropertyValueFactory<>("bookingID"));
        colEntrepreneur.setCellValueFactory(new PropertyValueFactory<>("entrepreneurID"));
        colMentor.setCellValueFactory(new PropertyValueFactory<>("mentorID"));
        colStartup.setCellValueFactory(new PropertyValueFactory<>("startupID"));
        colTopic.setCellValueFactory(new PropertyValueFactory<>("topic"));
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getRequestedDate() != null ? cd.getValue().getRequestedDate().toString() : ""));
        colTime.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getRequestedTime() != null ? cd.getValue().getRequestedTime().format(TF) : ""));
        colCreated.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getCreationDate() != null ? cd.getValue().getCreationDate().toString() : ""));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item.toLowerCase()) {
                    case "pending"  -> setStyle("-fx-text-fill:#c77a00;-fx-font-weight:700;");
                    case "approved" -> setStyle("-fx-text-fill:#12a059;-fx-font-weight:700;");
                    case "rejected" -> setStyle("-fx-text-fill:#e0134a;-fx-font-weight:700;");
                    default -> setStyle("");
                }
            }
        });
        loadTable();
    }

    @FXML private void onAdd() {
        clearMsg();
        Booking b = buildFromForm();
        if (b == null) return;
        try { service.add(b); showSuccess("Booking created!"); onClear(); loadTable(); }
        catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML private void onUpdate() {
        clearMsg();
        if (selected == null) { showError("Select a row to update."); return; }
        if ("approved".equals(selected.getStatus())) { showError("Approved bookings cannot be edited."); return; }
        Booking b = buildFromForm();
        if (b == null) return;
        b.setBookingID(selected.getBookingID());
        b.setStatus(selected.getStatus());
        try { service.update(b); showSuccess("Booking updated!"); onClear(); loadTable(); }
        catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML private void onDelete() {
        clearMsg();
        if (selected == null) { showError("Select a row to delete."); return; }
        try { service.delete(selected); showSuccess("Booking deleted!"); onClear(); loadTable(); }
        catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML private void onApprove() {
        clearMsg();
        if (selected == null) { showError("Select a booking to approve."); return; }
        try { service.approveBooking(selected); showSuccess("Approved! Session created automatically ✅"); onClear(); loadTable(); }
        catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML private void onReject() {
        clearMsg();
        if (selected == null) { showError("Select a booking to reject."); return; }
        try { service.rejectBooking(selected); showSuccess("Booking rejected."); onClear(); loadTable(); }
        catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML private void onClear() {
        entrepreneurIDField.clear(); mentorIDField.clear(); startupIDField.clear();
        requestedDatePicker.setValue(null); requestedTimeField.clear(); topicField.clear();
        clearMsg(); selected = null;
        bookingTable.getSelectionModel().clearSelection();
    }

    @FXML private void onRowSelected() {
        Booking b = bookingTable.getSelectionModel().getSelectedItem();
        if (b == null) return;
        selected = b;
        entrepreneurIDField.setText(String.valueOf(b.getEntrepreneurID()));
        mentorIDField.setText(String.valueOf(b.getMentorID()));
        startupIDField.setText(String.valueOf(b.getStartupID()));
        requestedDatePicker.setValue(b.getRequestedDate());
        requestedTimeField.setText(b.getRequestedTime() != null ? b.getRequestedTime().format(TF) : "");
        topicField.setText(b.getTopic());
    }

    @FXML private void onSearch() {
        String q = searchField.getText().toLowerCase();
        if (q.isBlank()) { bookingTable.setItems(allData); return; }
        List<Booking> f = allData.stream()
                .filter(b -> (b.getTopic() != null && b.getTopic().toLowerCase().contains(q))
                        || (b.getStatus() != null && b.getStatus().toLowerCase().contains(q)))
                .collect(Collectors.toList());
        bookingTable.setItems(FXCollections.observableArrayList(f));
    }

    private Booking buildFromForm() {
        int entID, mentorID, startupID;
        try { entID = Integer.parseInt(entrepreneurIDField.getText().trim()); }
        catch (NumberFormatException e) { showError("Entrepreneur ID must be a number."); return null; }
        try { mentorID = Integer.parseInt(mentorIDField.getText().trim()); }
        catch (NumberFormatException e) { showError("Mentor ID must be a number."); return null; }
        try { startupID = Integer.parseInt(startupIDField.getText().trim()); }
        catch (NumberFormatException e) { showError("Startup ID must be a number (use 1 if unsure)."); return null; }
        LocalDate date = requestedDatePicker.getValue();
        if (date == null) { showError("Please select a date."); return null; }
        LocalTime time;
        try { time = LocalTime.parse(requestedTimeField.getText().trim(), TF); }
        catch (DateTimeParseException e) { showError("Time must be HH:mm (e.g. 14:00)."); return null; }
        String topic = topicField.getText().trim();
        if (topic.isEmpty()) { showError("Topic is required."); return null; }
        return new Booking(entID, mentorID, startupID, date, time, topic);
    }

    private void loadTable() {
        allData = FXCollections.observableArrayList(service.list());
        bookingTable.setItems(allData);
    }

    private void showError(String msg)   { msgLabel.setText("⚠ " + msg); msgLabel.setStyle("-fx-text-fill:#e0134a;-fx-font-weight:700;"); }
    private void showSuccess(String msg) { msgLabel.setText("✓ " + msg); msgLabel.setStyle("-fx-text-fill:#12a059;-fx-font-weight:700;"); }
    private void clearMsg()              { msgLabel.setText(""); }
}
