package tn.esprit.GUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.Services.UserService;
import tn.esprit.entities.User;
import tn.esprit.utils.CurrentUserSession;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label msgLabel;

    private final UserService userService = new UserService();

    @FXML
    private void onLogin() {
        String email = emailField.getText().trim();
        String pass  = passwordField.getText();

        if (email.isEmpty() || pass.isEmpty()) {
            msgLabel.setText("Please fill email and password");
            return;
        }

        User u = userService.login(email, pass);

        if (u == null) {
            msgLabel.setText("Invalid email or password");
            return;
        }

        String st = (u.getStatus() == null) ? "PENDING" : u.getStatus().trim();

        if (st.equalsIgnoreCase("BLOCKED")) {
            msgLabel.setText("Your account is blocked");
            return;
        }

        // Save in session
        CurrentUserSession.user = u;

        // Redirect based on role
        String role = (u.getRole() == null) ? "" : u.getRole().trim().toUpperCase();

        if (role.equals("ADMIN")) {
            goTo("/DashboardAdmin.fxml");
        } else if (role.equals("ENTREPRENEUR")) {
            goTo("/EntrepreneurDashboard.fxml");
        } else if (role.equals("MENTOR")) {
            goTo("/MentorDashboard.fxml");
        } else if (role.equals("EVALUATOR")) {
            goTo("/EvaluatorDashboard.fxml");
        } else {
            msgLabel.setText("Unknown role: " + role);
        }
    }

    private void goTo(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            msgLabel.setText("Navigation error: " + e.getMessage());
        }
    }

    @FXML private void onGoSignup() { goTo("/signup.fxml"); }

    @FXML private void onLinkedin() {}
    @FXML private void onGoogle() {}
    @FXML private void onOther() {}

    @FXML private void togglePassword() {}
}