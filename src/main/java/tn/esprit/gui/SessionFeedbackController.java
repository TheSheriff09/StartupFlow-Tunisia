package tn.esprit.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entities.SessionFeedback;
import tn.esprit.services.SessionFeedbackService;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SessionFeedbackController implements Initializable {

    @FXML private TextField  sessionIDField;
    @FXML private TextField  mentorIDField;
    @FXML private DatePicker feedbackDatePicker;
    @FXML private Slider     scoreSlider;
    @FXML private Label      scoreLabel;
    @FXML private TextArea   strengthsArea;
    @FXML private TextArea   weaknessesArea;
    @FXML private TextArea   recommendationsArea;
    @FXML private TextArea   nextActionsArea;
    @FXML private Label      msgLabel;
    @FXML private TextField  searchField;

    @FXML private TableView<SessionFeedback>                feedbackTable;
    @FXML private TableColumn<SessionFeedback, Integer>     colID;
    @FXML private TableColumn<SessionFeedback, Integer>     colSession;
    @FXML private TableColumn<SessionFeedback, Integer>     colMentor;
    @FXML private TableColumn<SessionFeedback, Integer>     colScore;
    @FXML private TableColumn<SessionFeedback, String>      colStrengths;
    @FXML private TableColumn<SessionFeedback, String>      colWeaknesses;
    @FXML private TableColumn<SessionFeedback, String>      colRecommendations;
    @FXML private TableColumn<SessionFeedback, String>      colNextActions;
    @FXML private TableColumn<SessionFeedback, String>      colDate;

    private final SessionFeedbackService service = new SessionFeedbackService();
    private ObservableList<SessionFeedback> allData;
    private SessionFeedback selected;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        feedbackDatePicker.setValue(LocalDate.now());
        scoreSlider.valueProperty().addListener((obs, old, val) ->
                scoreLabel.setText(String.valueOf(val.intValue())));

        colID.setCellValueFactory(new PropertyValueFactory<>("feedbackID"));
        colSession.setCellValueFactory(new PropertyValueFactory<>("sessionID"));
        colMentor.setCellValueFactory(new PropertyValueFactory<>("mentorID"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("progressScore"));
        colStrengths.setCellValueFactory(new PropertyValueFactory<>("strengths"));
        colWeaknesses.setCellValueFactory(new PropertyValueFactory<>("weaknesses"));
        colRecommendations.setCellValueFactory(new PropertyValueFactory<>("recommendations"));
        colNextActions.setCellValueFactory(new PropertyValueFactory<>("nextActions"));
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getFeedbackDate() != null ? cd.getValue().getFeedbackDate().toString() : ""));

        colScore.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item.toString());
                if (item >= 75)      setStyle("-fx-text-fill:#12a059;-fx-font-weight:800;");
                else if (item >= 50) setStyle("-fx-text-fill:#c77a00;-fx-font-weight:800;");
                else                 setStyle("-fx-text-fill:#e0134a;-fx-font-weight:800;");
            }
        });
        loadTable();
    }

    @FXML private void onAdd() {
        clearMsg();
        SessionFeedback f = buildFromForm();
        if (f == null) return;
        try { service.add(f); showSuccess("Feedback submitted!"); onClear(); loadTable(); }
        catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML private void onUpdate() {
        clearMsg();
        if (selected == null) { showError("Select a row to update."); return; }
        SessionFeedback f = buildFromForm();
        if (f == null) return;
        f.setFeedbackID(selected.getFeedbackID());
        f.setSessionID(selected.getSessionID());
        try { service.update(f); showSuccess("Feedback updated!"); onClear(); loadTable(); }
        catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML private void onDelete() {
        clearMsg();
        if (selected == null) { showError("Select a row to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete this feedback?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try { service.delete(selected); showSuccess("Feedback deleted!"); onClear(); loadTable(); }
                catch (Exception e) { showError(e.getMessage()); }
            }
        });
    }

    @FXML private void onClear() {
        sessionIDField.clear(); mentorIDField.clear();
        feedbackDatePicker.setValue(LocalDate.now());
        scoreSlider.setValue(50); scoreLabel.setText("50");
        strengthsArea.clear(); weaknessesArea.clear();
        recommendationsArea.clear(); nextActionsArea.clear();
        clearMsg(); selected = null;
        feedbackTable.getSelectionModel().clearSelection();
    }

    @FXML private void onRowSelected() {
        SessionFeedback f = feedbackTable.getSelectionModel().getSelectedItem();
        if (f == null) return;
        selected = f;
        sessionIDField.setText(String.valueOf(f.getSessionID()));
        mentorIDField.setText(String.valueOf(f.getMentorID()));
        feedbackDatePicker.setValue(f.getFeedbackDate());
        scoreSlider.setValue(f.getProgressScore());
        strengthsArea.setText(f.getStrengths());
        weaknessesArea.setText(f.getWeaknesses());
        recommendationsArea.setText(f.getRecommendations());
        nextActionsArea.setText(f.getNextActions());
    }

    @FXML private void onSearch() {
        String q = searchField.getText().toLowerCase();
        if (q.isBlank()) { feedbackTable.setItems(allData); return; }
        List<SessionFeedback> f = allData.stream()
                .filter(fb -> String.valueOf(fb.getSessionID()).contains(q)
                        || String.valueOf(fb.getMentorID()).contains(q))
                .collect(Collectors.toList());
        feedbackTable.setItems(FXCollections.observableArrayList(f));
    }

    private SessionFeedback buildFromForm() {
        int sessionID, mentorID;
        try { sessionID = Integer.parseInt(sessionIDField.getText().trim()); }
        catch (NumberFormatException e) { showError("Session ID must be a number."); return null; }
        try { mentorID = Integer.parseInt(mentorIDField.getText().trim()); }
        catch (NumberFormatException e) { showError("Mentor ID must be a number."); return null; }
        if (feedbackDatePicker.getValue() == null) { showError("Feedback date is required."); return null; }
        if (strengthsArea.getText().trim().isEmpty()) { showError("Strengths field is required."); return null; }
        return new SessionFeedback(sessionID, mentorID, (int) scoreSlider.getValue(),
                strengthsArea.getText().trim(), weaknessesArea.getText().trim(),
                recommendationsArea.getText().trim(), nextActionsArea.getText().trim(),
                feedbackDatePicker.getValue());
    }

    private void loadTable() {
        allData = FXCollections.observableArrayList(service.list());
        feedbackTable.setItems(allData);
    }

    private void showError(String msg)   { msgLabel.setText("⚠ " + msg); msgLabel.setStyle("-fx-text-fill:#e0134a;-fx-font-weight:700;"); }
    private void showSuccess(String msg) { msgLabel.setText("✓ " + msg); msgLabel.setStyle("-fx-text-fill:#12a059;-fx-font-weight:700;"); }
    private void clearMsg()              { msgLabel.setText(""); }
}
