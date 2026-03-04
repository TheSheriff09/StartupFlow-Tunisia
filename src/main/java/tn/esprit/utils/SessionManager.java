package tn.esprit.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.entities.User;

/**
 * SessionManager — Single source of truth for the logged-in user session.
 *
 * Replaces the old bare {@code CurrentUserSession.user} static field with
 * a proper API that includes login/logout, role queries, and auto-redirect
 * guards for controller {@code initialize()} methods.
 */
public class SessionManager {

    private static User currentUser;

    // ── Login / Logout ──────────────────────────────────────

    /** Set the current user after successful authentication. */
    public static void login(User user) {
        currentUser = user;
        // Keep backward-compatible field in sync
        CurrentUserSession.user = user;
    }

    /** Clear the session completely. */
    public static void logout() {
        currentUser = null;
        CurrentUserSession.user = null;
    }

    // ── Getters ─────────────────────────────────────────────

    /** Get the currently logged-in user, or {@code null} if not logged in. */
    public static User getUser() {
        // Stay in sync with legacy field
        if (currentUser == null && CurrentUserSession.user != null) {
            currentUser = CurrentUserSession.user;
        }
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return getUser() != null;
    }

    /**
     * Returns the user's role in upper-case, or empty string if unknown.
     */
    public static String getRole() {
        User u = getUser();
        if (u == null || u.getRole() == null)
            return "";
        return u.getRole().trim().toUpperCase();
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(getRole());
    }

    public static boolean isEntrepreneur() {
        return "ENTREPRENEUR".equals(getRole());
    }

    public static boolean isMentor() {
        return "MENTOR".equals(getRole());
    }

    public static boolean isEvaluator() {
        return "EVALUATOR".equals(getRole());
    }

    // ── Guards (for use in controller initialize()) ──────────

    /**
     * Ensures a user is logged in. If not, redirects to the Login page
     * and returns {@code false}. Call at the top of {@code initialize()}.
     *
     * <pre>
     * {@code
     * if (!SessionManager.requireLogin(someNode))
     *     return;
     * }
     * </pre>
     *
     * @param sceneNode any node currently in the scene graph
     * @return true if user is logged in, false if redirected
     */
    public static boolean requireLogin(Node sceneNode) {
        if (isLoggedIn())
            return true;
        // Defer redirect to after the scene is fully initialised
        javafx.application.Platform.runLater(() -> NavigationManager.navigateTo(sceneNode, "/Login.fxml"));
        return false;
    }

    /**
     * Ensures the logged-in user has one of the required roles.
     * If not, redirects to the appropriate dashboard and returns {@code false}.
     *
     * @param sceneNode    any node currently in the scene graph
     * @param allowedRoles one or more role strings (upper-case), e.g. "ADMIN"
     * @return true if the user has a matching role, false if redirected
     */
    public static boolean requireRole(Node sceneNode, String... allowedRoles) {
        if (!requireLogin(sceneNode))
            return false;
        String role = getRole();
        for (String r : allowedRoles) {
            if (r.equalsIgnoreCase(role))
                return true;
        }
        // Unauthorized — redirect to their own dashboard
        javafx.application.Platform.runLater(() -> NavigationManager.goToDashboard(sceneNode));
        return false;
    }

    /**
     * Returns the display name for the top bar.
     */
    public static String getDisplayName() {
        User u = getUser();
        if (u == null)
            return "User";
        String name = u.getFullName();
        return (name == null || name.isBlank()) ? "User" : name.trim();
    }
}
