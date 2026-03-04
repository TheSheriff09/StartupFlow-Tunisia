package tn.esprit.utils;

import javafx.scene.control.TextFormatter;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ValidationUtil — Strict, reusable input validation rules.
 *
 * Each {@code check*()} method returns an {@code Optional<String>}:
 *   - {@code Optional.empty()} → field is valid
 *   - {@code Optional.of("Error message")} → field fails with that message
 *
 * Usage pattern:
 * <pre>
 *   List&lt;String&gt; errors = new ArrayList&lt;&gt;();
 *   ValidationUtil.checkName("TechCorp", "Startup Name", existingNames)
 *       .ifPresent(errors::add);
 *   ValidationUtil.checkPositiveDouble("0", "Funding", true)
 *       .ifPresent(errors::add);
 *   if (!errors.isEmpty()) {
 *       AlertUtil.showValidationErrors(errors, owner);
 *       return;
 *   }
 * </pre>
 *
 * All "mark on field" helpers are purely visual — they apply the same
 * red border / error label styling as {@link FormValidator}.
 */
public final class ValidationUtil {

    // ── Shared UI style constants — dynamically resolved via DesignTokens ─
    private static String fieldNormal() { return DesignTokens.fieldNormal(); }
    private static String fieldError()  { return DesignTokens.fieldError(); }
    private static String labelError()  { return DesignTokens.errorLabel(); }

    // Minimum / maximum character limits for name / text fields
    public static final int MIN_TEXT_LENGTH      = 3;
    public static final int MAX_TEXT_LENGTH      = 100;
    public static final int MAX_DESCRIPTION_LEN  = 2000;

    private ValidationUtil() {}

    // ─────────────────────────────────────────────────────────
    // Text / Name rules
    // ─────────────────────────────────────────────────────────

    /**
     * Full name validation:
     *   1. Not blank
     *   2. Min 3 characters
     *   3. Max 100 characters
     *   4. Must not be entirely numeric (e.g. "12345")
     *   5. Optional duplicate check against an existing-names list
     *
     * @param value         the trimmed input value
     * @param fieldLabel    human-readable field name used in the error message
     * @param existingNames list of already-saved names (case-insensitive check).
     *                      Pass an empty list if no duplicate check is needed.
     * @param excludeId     pass a non-null, non-blank value to exclude one entry
     *                      (used when editing — pass the entity's own current name)
     */
    public static Optional<String> checkName(String value,
                                             String fieldLabel,
                                             List<String> existingNames,
                                             String excludeCurrentName) {
        String v = (value == null) ? "" : value.trim();

        if (v.isEmpty())
            return Optional.of(fieldLabel + " is required.");

        if (v.length() < MIN_TEXT_LENGTH)
            return Optional.of(fieldLabel + " must be at least " + MIN_TEXT_LENGTH + " characters.");

        if (v.length() > MAX_TEXT_LENGTH)
            return Optional.of(fieldLabel + " must not exceed " + MAX_TEXT_LENGTH + " characters.");

        if (v.matches("\\d+"))
            return Optional.of(fieldLabel + " cannot be only numeric digits.");

        if (existingNames != null) {
            String lc = v.toLowerCase();
            String excl = (excludeCurrentName != null) ? excludeCurrentName.trim().toLowerCase() : "";
            for (String existing : existingNames) {
                if (existing == null) continue;
                String el = existing.trim().toLowerCase();
                if (el.equals(lc) && !el.equals(excl))
                    return Optional.of(fieldLabel + " \"" + v + "\" already exists. Please choose a different name.");
            }
        }

        return Optional.empty();
    }

    /**
     * Generic required text field (non-blank, min 3, max 100).
     * Does not enforce the no-only-digits rule (use {@link #checkName} for names).
     */
    public static Optional<String> checkTextRequired(String value, String fieldLabel) {
        String v = (value == null) ? "" : value.trim();
        if (v.isEmpty())   return Optional.of(fieldLabel + " is required.");
        if (v.length() < MIN_TEXT_LENGTH)
            return Optional.of(fieldLabel + " must be at least " + MIN_TEXT_LENGTH + " characters.");
        if (v.length() > MAX_TEXT_LENGTH)
            return Optional.of(fieldLabel + " must not exceed " + MAX_TEXT_LENGTH + " characters.");
        return Optional.empty();
    }

    /**
     * Optional text field — only validates length if a value is provided.
     */
    public static Optional<String> checkTextOptional(String value, String fieldLabel, int maxLen) {
        String v = (value == null) ? "" : value.trim();
        if (v.isEmpty())         return Optional.empty(); // optional — blank is fine
        if (v.length() < MIN_TEXT_LENGTH)
            return Optional.of(fieldLabel + " must be at least " + MIN_TEXT_LENGTH + " characters if provided.");
        if (v.length() > maxLen)
            return Optional.of(fieldLabel + " must not exceed " + maxLen + " characters.");
        return Optional.empty();
    }

    // ─────────────────────────────────────────────────────────
    // Numeric rules
    // ─────────────────────────────────────────────────────────

    /**
     * Validates a double field.
     * - If {@code required=true}: empty string fails
     * - Value must be > 0 (strictly positive)
     *
     * @param value      the raw text from the input control
     * @param fieldLabel label for the error message
     * @param required   if true, a blank value is an error
     */
    public static Optional<String> checkPositiveDouble(String value,
                                                       String fieldLabel,
                                                       boolean required) {
        String v = (value == null) ? "" : value.trim();
        if (v.isEmpty()) {
            if (required) return Optional.of(fieldLabel + " is required.");
            return Optional.empty();
        }
        try {
            double d = Double.parseDouble(v);
            if (d <= 0)
                return Optional.of(fieldLabel + " must be a positive number (> 0).");
        } catch (NumberFormatException e) {
            return Optional.of(fieldLabel + " must be a valid number (e.g. 50000).");
        }
        return Optional.empty();
    }

    /**
     * Validates that the growth rate is a number between 0 and 100 inclusive.
     * The field is required (blank fails).
     */
    public static Optional<String> checkGrowthRate(String value) {
        String v = (value == null) ? "" : value.trim();
        if (v.isEmpty()) return Optional.of("Growth Rate is required.");
        try {
            double d = Double.parseDouble(v);
            if (d < 0 || d > 100)
                return Optional.of("Growth Rate must be between 0 and 100.");
        } catch (NumberFormatException e) {
            return Optional.of("Growth Rate must be a valid number (e.g. 5.5).");
        }
        return Optional.empty();
    }

    /**
     * Validates a KPI score — optional double between 0 and 10.
     * Empty is permitted (returns empty Optional).
     */
    public static Optional<String> checkKpiScore(String value) {
        String v = (value == null) ? "" : value.trim();
        if (v.isEmpty()) return Optional.empty(); // optional
        try {
            double d = Double.parseDouble(v);
            if (d < 0 || d > 10)
                return Optional.of("KPI Score must be between 0 and 10.");
        } catch (NumberFormatException e) {
            return Optional.of("KPI Score must be a valid number (e.g. 8.5).");
        }
        return Optional.empty();
    }

    /**
     * Validates a funding amount — optional positive double.
     * If provided, it must be > 0.
     */
    public static Optional<String> checkFunding(String value) {
        return checkPositiveDouble(value, "Funding Amount", false);
    }

    /**
     * Text formatter that accepts only unsigned decimal values (e.g. 12, 12.5).
     * Useful for funding, revenue, expenses, and growth-rate fields.
     */
    public static TextFormatter<String> unsignedDecimalFormatter() {
        return new TextFormatter<>(change -> {
            String text = change.getControlNewText();
            return (text.isEmpty() || text.matches("\\d*\\.?\\d*")) ? change : null;
        });
    }

    // ─────────────────────────────────────────────────────────
    // ComboBox / selection rules
    // ─────────────────────────────────────────────────────────

    /**
     * Returns an error if the selection is null.
     *
     * @param value      the selected value (may be null)
     * @param fieldLabel label for the error message
     */
    public static Optional<String> checkComboSelected(Object value, String fieldLabel) {
        if (value == null)
            return Optional.of(fieldLabel + " selection is required.");
        return Optional.empty();
    }

    // ─────────────────────────────────────────────────────────
    // Field highlighting helpers
    // ─────────────────────────────────────────────────────────

    /**
     * Marks a text field and its error label as invalid with the given message.
     * Call this alongside inline validation to highlight the offending control.
     */
    public static void markFieldError(TextInputControl field, Label errLbl, String msg) {
        if (field != null)  field.setStyle(fieldError());
        if (errLbl != null) {
            errLbl.setText(msg);
            errLbl.setStyle(labelError());
            errLbl.setVisible(true);
            errLbl.setManaged(true);
        }
    }

    /** Clears the error state on a field and its error label. */
    public static void clearFieldError(TextInputControl field, Label errLbl) {
        if (field != null)  field.setStyle(fieldNormal());
        if (errLbl != null) {
            errLbl.setText("");
            errLbl.setVisible(false);
            errLbl.setManaged(false);
        }
    }

    // ─────────────────────────────────────────────────────────
    // Batch helpers
    // ─────────────────────────────────────────────────────────

    /**
     * Adds the error message from the given Optional to the errors list (if present).
     *
     * <pre>
     *   List&lt;String&gt; errors = new ArrayList&lt;&gt;();
     *   ValidationUtil.collect(ValidationUtil.checkName(...), errors);
     * </pre>
     */
    public static void collect(Optional<String> result, List<String> errors) {
        result.ifPresent(errors::add);
    }

    /**
     * Returns {@code true} if the list is empty (all checks passed).
     * Convenience to invert {@link List#isEmpty()}.
     */
    public static boolean allPassed(List<String> errors) {
        return errors.isEmpty();
    }

    /**
     * Formats the error list as a bullet-point string for alert display.
     *
     * <pre>
     *   • Startup Name is required.
     *   • Funding Amount must be a positive number (> 0).
     * </pre>
     */
    public static String formatErrors(List<String> errors) {
        StringBuilder sb = new StringBuilder();
        for (String e : errors) sb.append("• ").append(e).append("\n");
        return sb.toString().trim();
    }

    /**
     * Collects all errors from the given {@code Optional<String>} varargs and
     * returns them as an ordered list.  This is a shorthand for many individual
     * {@link #collect} calls:
     * <pre>
     *   List&lt;String&gt; errors = ValidationUtil.gatherErrors(
     *       checkName(...), checkFunding(...), checkGrowthRate(...)
     *   );
     * </pre>
     */
    @SafeVarargs
    public static List<String> gatherErrors(Optional<String>... checks) {
        List<String> errors = new ArrayList<>();
        for (Optional<String> c : checks) c.ifPresent(errors::add);
        return errors;
    }
}

