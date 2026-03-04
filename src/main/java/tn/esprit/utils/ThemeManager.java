package tn.esprit.utils;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;

import java.util.prefs.Preferences;

/**
 * ThemeManager — Singleton that manages dark / light theme switching.
 *
 * <ul>
 *   <li>Stores current theme in user preferences (persists across launches).</li>
 *   <li>Applies or removes {@code light.css} from any Scene or DialogPane.</li>
 *   <li>Provides {@link #isDark()} for theme-aware inline-style decisions.</li>
 * </ul>
 *
 * Usage:
 * <pre>
 *   // On navigation — apply theme to the new scene
 *   ThemeManager.getInstance().applyTo(scene);
 *
 *   // Toggle from a button handler
 *   ThemeManager.getInstance().toggle();
 *   ThemeManager.getInstance().applyTo(btn.getScene());
 *
 *   // Query for inline style decisions
 *   if (ThemeManager.getInstance().isDark()) { ... }
 * </pre>
 */
public final class ThemeManager {

    public enum Theme { DARK, LIGHT }

    private static final ThemeManager INSTANCE = new ThemeManager();
    private static final String PREF_KEY = "startupflow.theme";

    private Theme currentTheme;
    private final String lightCssUrl;

    private ThemeManager() {
        // Restore saved preference (default = DARK)
        Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
        String saved = prefs.get(PREF_KEY, "DARK");
        currentTheme = "LIGHT".equals(saved) ? Theme.LIGHT : Theme.DARK;

        // Cache the light.css URL once
        var url = getClass().getResource("/light.css");
        lightCssUrl = url != null ? url.toExternalForm() : null;
    }

    /** Returns the global singleton. */
    public static ThemeManager getInstance() { return INSTANCE; }

    /** Current theme enum value. */
    public Theme getTheme() { return currentTheme; }

    /** {@code true} when the dark theme is active. */
    public boolean isDark() { return currentTheme == Theme.DARK; }

    /** Switch to a specific theme and persist the choice. */
    public void setTheme(Theme theme) {
        this.currentTheme = theme;
        Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
        prefs.put(PREF_KEY, theme.name());
    }

    /** Toggle between dark and light, persisting the new choice. */
    public void toggle() {
        setTheme(isDark() ? Theme.LIGHT : Theme.DARK);
    }

    /**
     * Apply the current theme to a Scene by adding / removing {@code light.css}
     * on the scene's root node (same level as the FXML {@code stylesheets} attribute).
     */
    public void applyTo(Scene scene) {
        if (scene == null || lightCssUrl == null) return;
        Parent root = scene.getRoot();
        if (root == null) return;
        if (isDark()) {
            root.getStylesheets().remove(lightCssUrl);
        } else {
            if (!root.getStylesheets().contains(lightCssUrl)) {
                root.getStylesheets().add(lightCssUrl);
            }
        }
    }

    /**
     * Apply the current theme to a DialogPane's stylesheets
     * (used by DialogStyler and AlertUtil).
     */
    public void applyTo(DialogPane pane) {
        if (pane == null || lightCssUrl == null) return;
        if (isDark()) {
            pane.getStylesheets().remove(lightCssUrl);
        } else {
            if (!pane.getStylesheets().contains(lightCssUrl)) {
                pane.getStylesheets().add(lightCssUrl);
            }
        }
    }

    /** Returns the external-form URL of {@code light.css}, or {@code null}. */
    public String getLightCssUrl() { return lightCssUrl; }
}

