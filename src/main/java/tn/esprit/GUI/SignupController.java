package tn.esprit.GUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.utils.SignupSession;

public class SignupController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label msgLabel; // optional if you add it later
    @FXML private Button togglePassBtn;
    @FXML private TextField visiblePasswordField;
    private boolean showingPassword = false;
    @FXML
    private void register() {
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String pass = passwordField.getText();
        String password = showingPassword
                ? visiblePasswordField.getText()
                : passwordField.getText();
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            System.out.println("Please fill all fields");
            return;
        }

        // Save temporarily
        SignupSession.fullName = fullName;
        SignupSession.email = email;
        SignupSession.passwordPlain = password;
        System.out.println(getClass().getResource("/RoleChoice.fxml"));
        goTo("RoleChoice.fxml");

    }

    private void goTo(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent root = loader.load();

            Stage stage = (Stage) fullNameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML private void togglePassword() {
        if (showingPassword) {
            // Switch back to hidden mode
            passwordField.setText(visiblePasswordField.getText());

            passwordField.setVisible(true);
            passwordField.setManaged(true);

            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);

            togglePassBtn.setText("👁");

            showingPassword = false;

        } else {
            // Switch to visible mode
            visiblePasswordField.setText(passwordField.getText());

            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);

            passwordField.setVisible(false);
            passwordField.setManaged(false);

            togglePassBtn.setText("🙈");

            showingPassword = true;
        }
    }

    @FXML private void socialFacebook() {}
    @FXML private void socialGoogle() {}
    @FXML private void socialGithub() {}
    @FXML private void goLogin() {        goTo("Login.fxml");
    }
}