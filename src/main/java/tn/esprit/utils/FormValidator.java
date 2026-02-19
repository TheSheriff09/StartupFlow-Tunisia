package tn.esprit.utils;

import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

/**
 * FormValidator — Reusable real-time input validation utility.
 *
 * Applies the project's purple-theme error UI (red border + small error label)
 * to any TextInputControl or DatePicker. Designed for use in both the
 * Startup and BusinessPlan CRUD flows.
 *
 * Usage:
 *   boolean ok = FormValidator.requireNonEmpty(tfName, errName);
 *   boolean ok = FormValidator.requireDouble(tfFunding, errFunding, false);
 */
public final class FormValidator {

    // ── Error style tokens ────────────────────────────────────
    private static final String NORMAL_STYLE =
        "-fx-background-color: rgba(255,255,255,0.92);" +
        "-fx-text-fill: #3b1f6b;" +
        "-fx-border-color: rgba(167,139,250,0.58);" +
        "-fx-border-radius: 12;" +
        "-fx-background-radius: 12;" +
        "-fx-border-width: 1.5;" +
        "-fx-prompt-text-fill: rgba(139,92,246,0.46);" +
        "-fx-font-size: 13px;" +
        "-fx-padding: 11 16 11 16;";

    private static final String ERROR_STYLE =
        "-fx-background-color: rgba(255,235,235,0.95);" +
        "-fx-text-fill: #7f1d1d;" +
        "-fx-border-color: #f87171;" +
        "-fx-border-radius: 12;" +
        "-fx-background-radius: 12;" +
        "-fx-border-width: 2;" +
        "-fx-prompt-text-fill: rgba(239,68,68,0.50);" +
        "-fx-font-size: 13px;" +
        "-fx-padding: 11 16 11 16;";

    private static final String ERROR_LABEL_STYLE =
        "-fx-text-fill: #ef4444;" +
        "-fx-font-size: 10.5px;" +
        "-fx-font-weight: 700;" +
        "-fx-padding: 2 0 0 2;";

    private FormValidator() {}

    // ─────────────────────────────────────────────────────────
    // Public validation methods
    // ─────────────────────────────────────────────────────────

    /**
     * Validates that a text field is not blank.
     *
     * @param field  the control to validate
     * @param errLbl the Label below the field for the error message (may be null)
     * @return true if valid
     */
    public static boolean requireNonEmpty(TextInputControl field, Label errLbl) {
        String val = field.getText() == null ? "" : field.getText().trim();
        if (val.isEmpty()) {
            markError(field, errLbl, "This field is required.");
            return false;
        }
        clearError(field, errLbl);
        return true;
    }

    /**
     * Validates text does not exceed {@code max} characters.
     */
    public static boolean requireMaxLength(TextInputControl field, Label errLbl, int max) {
        String val = field.getText() == null ? "" : field.getText();
        if (val.length() > max) {
            markError(field, errLbl, "Maximum " + max + " characters allowed.");
            return false;
        }
        clearError(field, errLbl);
        return true;
    }

    /**
     * Validates that a field (if non-empty) contains a valid double.
     *
     * @param required if true, an empty field also fails
     */
    public static boolean requireDouble(TextInputControl field, Label errLbl, boolean required) {
        String val = field.getText() == null ? "" : field.getText().trim();
        if (val.isEmpty()) {
            if (required) {
                markError(field, errLbl, "This field is required.");
                return false;
            }
            clearError(field, errLbl);
            return true;
        }
        try {
            double d = Double.parseDouble(val);
            if (d < 0) {
                markError(field, errLbl, "Value must be ≥ 0.");
                return false;
            }
            clearError(field, errLbl);
            return true;
        } catch (NumberFormatException e) {
            markError(field, errLbl, "Enter a valid number (e.g. 50000).");
            return false;
        }
    }

    /**
     * Validates a KPI score: optional double between 0 and 10.
     */
    public static boolean requireKpiScore(TextInputControl field, Label errLbl) {
        String val = field.getText() == null ? "" : field.getText().trim();
        if (val.isEmpty()) {
            clearError(field, errLbl);
            return true;
        }
        try {
            double d = Double.parseDouble(val);
            if (d < 0 || d > 10) {
                markError(field, errLbl, "KPI score must be between 0 and 10.");
                return false;
            }
            clearError(field, errLbl);
            return true;
        } catch (NumberFormatException e) {
            markError(field, errLbl, "Enter a valid number (e.g. 8.5).");
            return false;
        }
    }

    /**
     * Validates a DatePicker is not empty and not in the future (when {@code noFuture=true}).
     */
    public static boolean requireDate(DatePicker dp, Label errLbl, boolean noFuture) {
        if (dp.getValue() == null) {
            markDateError(dp, errLbl, "Please select a date.");
            return false;
        }
        if (noFuture && dp.getValue().isAfter(LocalDate.now())) {
            markDateError(dp, errLbl, "Date cannot be in the future.");
            return false;
        }
        clearDateError(dp, errLbl);
        return true;
    }

    /**
     * Validates that an image file path ends with a supported extension.
     * Always returns true if the path is null/empty (image is optional).
     */
    public static boolean requireImageExtension(String path, Label errLbl) {
        if (path == null || path.isBlank()) {
            if (errLbl != null) { errLbl.setText(""); errLbl.setVisible(false); }
            return true;
        }
        String lower = path.toLowerCase();
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            if (errLbl != null) { errLbl.setText(""); errLbl.setVisible(false); }
            return true;
        }
        if (errLbl != null) {
            errLbl.setText("Only PNG, JPG or JPEG images are accepted.");
            errLbl.setStyle(ERROR_LABEL_STYLE);
            errLbl.setVisible(true);
            errLbl.setManaged(true);
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────
    // Real-time listener helpers
    // ─────────────────────────────────────────────────────────

    /**
     * Attaches a real-time listener: clears the error as soon as the user types anything.
     */
    public static void clearOnType(TextInputControl field, Label errLbl) {
        field.textProperty().addListener((obs, o, n) -> {
            if (n != null && !n.trim().isEmpty()) clearError(field, errLbl);
        });
    }

    /**
     * Attaches a real-time numeric listener: validates live as the user types.
     */
    public static void validateDoubleOnType(TextInputControl field, Label errLbl, boolean required) {
        field.textProperty().addListener((obs, o, n) -> requireDouble(field, errLbl, required));
    }

    /**
     * Enforces a maximum length by trimming excess characters as the user types.
     */
    public static void enforceLengthLimit(TextInputControl field, int max) {
        field.textProperty().addListener((obs, o, n) -> {
            if (n != null && n.length() > max)
                field.setText(n.substring(0, max));
        });
    }

    // ─────────────────────────────────────────────────────────
    // Error label factory
    // ─────────────────────────────────────────────────────────

    /**
     * Creates a styled error Label, initially invisible.
     * Typically added to a VBox just below the input field.
     */
    public static Label errorLabel() {
        Label lbl = new Label();
        lbl.setStyle(ERROR_LABEL_STYLE);
        lbl.setVisible(false);
        lbl.setManaged(false);
        lbl.setWrapText(true);
        return lbl;
    }

    /**
     * Creates a VBox field row: label + input control + hidden error label.
     */
    public static VBox fieldRow(String labelText, javafx.scene.Node field, Label errLbl) {
        Label lbl = new Label(labelText);
        lbl.setStyle(
            "-fx-text-fill: rgba(233,213,255,0.85);" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;");
        VBox row = errLbl != null
            ? new VBox(5, lbl, field, errLbl)
            : new VBox(5, lbl, field);
        row.setStyle("-fx-background-color: transparent;");
        return row;
    }

    // ─────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────

    private static void markError(TextInputControl field, Label errLbl, String msg) {
        field.setStyle(ERROR_STYLE);
        if (errLbl != null) {
            errLbl.setText(msg);
            errLbl.setStyle(ERROR_LABEL_STYLE);
            errLbl.setVisible(true);
            errLbl.setManaged(true);
        }
    }

    private static void clearError(TextInputControl field, Label errLbl) {
        field.setStyle(NORMAL_STYLE);
        if (errLbl != null) {
            errLbl.setText("");
            errLbl.setVisible(false);
            errLbl.setManaged(false);
        }
    }

    private static void markDateError(DatePicker dp, Label errLbl, String msg) {
        dp.setStyle(
            "-fx-background-color: rgba(255,235,235,0.95);" +
            "-fx-border-color: #f87171;" +
            "-fx-border-radius: 12;-fx-background-radius: 12;" +
            "-fx-border-width: 2;-fx-font-size: 13px;-fx-padding: 3 0 3 6;");
        if (errLbl != null) {
            errLbl.setText(msg);
            errLbl.setStyle(ERROR_LABEL_STYLE);
            errLbl.setVisible(true);
            errLbl.setManaged(true);
        }
    }

    private static void clearDateError(DatePicker dp, Label errLbl) {
        dp.setStyle(
            "-fx-background-color: rgba(255,255,255,0.90);" +
            "-fx-border-color: rgba(167,139,250,0.55);" +
            "-fx-border-radius: 12;-fx-background-radius: 12;" +
            "-fx-border-width: 1.5;-fx-font-size: 13px;-fx-padding: 3 0 3 6;");
        if (errLbl != null) {
            errLbl.setText("");
            errLbl.setVisible(false);
            errLbl.setManaged(false);
        }
    }
}
