package tn.esprit.utils;

import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import tn.esprit.entities.User;

/**
 * TopBarHelper — Reusable setup for the top-right user dropdown.
 *
 * Every controller that has a {@code userMenuBtn} / {@code MenuButton}
 * with Profile, My Startups, Settings, Logout items can call
 * {@link #setup} once in {@code initialize()} to get consistent behavior.
 */
public class TopBarHelper {

    /**
     * Set up the user dropdown menu with dynamic username and consistent actions.
     *
     * @param userMenuBtn the top-right MenuButton (shows username)
     * @param miHeader    optional header MenuItem (displays full name)
     * @param anyNode     any node in the scene graph (for navigation)
     */
    public static void setup(MenuButton userMenuBtn, MenuItem miHeader, Node anyNode) {
        if (userMenuBtn == null)
            return;

        String displayName = SessionManager.getDisplayName();
        userMenuBtn.setText(displayName);

        if (miHeader != null) {
            miHeader.setText(displayName);
        }

        // Clear existing menu items and rebuild with consistent options
        userMenuBtn.getItems().clear();

        // Header item (non-interactive, shows username)
        MenuItem header = new MenuItem(displayName);
        header.setDisable(true);
        header.getStyleClass().add("dropdown-header");

        // Profile
        MenuItem miProfile = new MenuItem("👤  Profile");
        miProfile.setOnAction(e -> {
            NavContext.setBack(getCurrentFxml(anyNode));
            NavigationManager.navigateTo(anyNode, "/ManageProfile.fxml");
        });

        // My Startups
        MenuItem miStartups = new MenuItem("🚀  My Startups");
        miStartups.setOnAction(e -> {
            if (SessionManager.isAdmin()) {
                NavigationManager.navigateTo(anyNode, "/admin/adminstartups.fxml");
            } else {
                NavigationManager.navigateTo(anyNode, "/startupview.fxml");
            }
        });

        // Settings
        MenuItem miSettings = new MenuItem("⚙  Settings");
        miSettings.setOnAction(e -> {
            System.out.println("[TopBarHelper] Settings clicked — not yet implemented");
        });

        // Separator-like spacer
        MenuItem separator = new MenuItem("─────────────");
        separator.setDisable(true);
        separator.getStyleClass().add("menu-separator");

        // Logout
        MenuItem miLogout = new MenuItem("🚪  Logout");
        miLogout.getStyleClass().add("logoutItem");
        miLogout.setOnAction(e -> NavigationManager.logout(anyNode));

        userMenuBtn.getItems().addAll(header, miProfile, miStartups, miSettings, separator, miLogout);
    }

    /**
     * Tries to determine the current FXML path for back-navigation.
     * Falls back to the entrepreneur dashboard.
     */
    private static String getCurrentFxml(Node node) {
        // Use NavContext.backFxml as a reasonable default
        return NavContext.backFxml;
    }
}
