package tn.esprit.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * NavigationManager — Centralized scene-switching utility.
 *
 * Eliminates the duplicate {@code goTo()} methods that existed in every
 * controller. All FXML navigation now flows through this single class.
 */
public class NavigationManager {

    /**
     * Navigate to an FXML page using any node currently in the scene graph.
     *
     * @param currentNode any node on the current scene (used to obtain the Stage)
     * @param fxmlPath    classpath-relative FXML path, e.g. "/ForumFeed.fxml"
     */
    public static void navigateTo(Node currentNode, String fxmlPath) {
        if (currentNode == null || currentNode.getScene() == null) {
            System.err.println("[NavigationManager] Cannot navigate — node or scene is null");
            return;
        }
        Stage stage = (Stage) currentNode.getScene().getWindow();
        navigateTo(stage, fxmlPath);
    }

    /**
     * Navigate to an FXML page using a Stage reference directly.
     */
    public static void navigateTo(Stage stage, String fxmlPath) {
        try {
            java.net.URL url = NavigationManager.class.getResource(fxmlPath);
            if (url == null) {
                System.err.println("[NavigationManager] FXML NOT FOUND: " + fxmlPath);
                return;
            }
            Parent root = FXMLLoader.load(url);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("[NavigationManager] Error navigating to " + fxmlPath
                    + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigate to the role-appropriate dashboard for the current user.
     * Falls back to Login page if no user is logged in.
     */
    public static void goToDashboard(Node currentNode) {
        if (!SessionManager.isLoggedIn()) {
            navigateTo(currentNode, "/Login.fxml");
            return;
        }
        String role = SessionManager.getRole();
        String target = switch (role) {
            case "ADMIN" -> "/DashboardAdmin.fxml";
            case "ENTREPRENEUR" -> "/EntrepreneurDashboard.fxml";
            case "MENTOR" -> "/MentorDashboard.fxml";
            case "EVALUATOR" -> "/EvaluatorDashboard.fxml";
            default -> "/EntrepreneurDashboard.fxml";
        };
        navigateTo(currentNode, target);
    }

    /**
     * Logout: clear session and navigate to the Login page.
     */
    public static void logout(Node currentNode) {
        SessionManager.logout();
        navigateTo(currentNode, "/Login.fxml");
    }
}
