package tn.esprit.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MentorshipDashboardController implements Initializable {

    @FXML private StackPane mainContent;
    @FXML private Label     topTitle;
    @FXML private Button    btnSchedule;
    @FXML private Button    btnBookings;
    @FXML private Button    btnSessions;
    @FXML private Button    btnFeedback;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showSchedule();
    }

    @FXML
    public void showSchedule() {
        setActive(btnSchedule);
        topTitle.setText("Mentorship — Schedule");
        loadView("/fxml/ScheduleView.fxml");
    }

    @FXML
    public void showBookings() {
        setActive(btnBookings);
        topTitle.setText("Mentorship — Booking Requests");
        loadView("/fxml/BookingView.fxml");
    }

    @FXML
    public void showSessions() {
        setActive(btnSessions);
        topTitle.setText("Mentorship — Sessions");
        loadView("/fxml/SessionView.fxml");
    }

    @FXML
    public void showFeedback() {
        setActive(btnFeedback);
        topTitle.setText("Mentorship — Feedback");
        loadView("/fxml/FeedbackView.fxml");
    }

    private void loadView(String fxml) {
        try {
            Node node = FXMLLoader.load(getClass().getResource(fxml));
            mainContent.getChildren().setAll(node);
        } catch (IOException e) {
            System.err.println("Cannot load view " + fxml + ": " + e.getMessage());
        }
    }

    private void setActive(Button active) {
        for (Button b : new Button[]{btnSchedule, btnBookings, btnSessions, btnFeedback}) {
            if (b != null) b.getStyleClass().remove("active");
        }
        if (active != null) active.getStyleClass().add("active");
    }
}
