package tn.esprit.utils;

/**
 * DesignTokens — Single source of truth for all inline style constants.
 *
 * ╔════════════════════════════════════════════════════════════════╗
 * ║  STARTUPFLOW DESIGN SYSTEM v2.0                               ║
 * ║  Dark Theme · Financial-Forecast-Inspired · Deep Purple       ║
 * ╚════════════════════════════════════════════════════════════════╝
 *
 * COLOR PALETTE
 * ─────────────────────────────────────────────────────────────────
 *   Primary (violet)     : #6d28d9  — gradient start, primary actions
 *   Primary dark         : #4c1d95  — deep accents
 *   Primary medium       : #7c3aed  — focus rings, accent buttons
 *   Primary light        : #8b5cf6  — icons, secondary emphasis
 *   Primary pale         : #a78bfa  — labels, light text on dark
 *   Primary soft         : #c4b5fd  — headings, counts on dark bg
 *   Primary ghost        : rgba(139,92,246,0.30) — field borders
 *
 *   Success              : #059669  — confirmations, active status
 *   Danger               : #ef4444  — errors, delete operations
 *   Warning              : #d97706  — caution messages
 *   Info                 : #0369a1  — informational dialogs
 *
 *   Dark body            : #1a0f3c  — card/modal background
 *   Dark gradient start  : #0d0820  — root pane gradient top
 *   Dark gradient end    : #120a2e  — root pane gradient bottom
 *   Dark glass field     : rgba(255,255,255,0.07)
 *   Dark glass border    : rgba(139,92,246,0.25)
 *   Dark error bg        : rgba(239,68,68,0.10)
 *
 *   Text heading         : white
 *   Text body            : rgba(220,210,255,0.95)
 *   Text muted           : rgba(200,190,235,0.70)
 *   Text faint           : rgba(167,139,250,0.42)
 *   Text on dark         : rgba(220,210,255,0.95)
 *
 * TYPOGRAPHY
 * ─────────────────────────────────────────────────────────────────
 *   Font family          : Segoe UI, Inter, Helvetica Neue, Arial
 *   Title (page)         : 26px / 900
 *   Title (card)         : 19px / 900
 *   Title (dialog)       : 18px / 900
 *   Title (section)      : 14px / 800
 *   Body                 : 13px / 600
 *   Label (form)         : 11px / bold
 *   Caption / badge      : 10px / 800
 *   Error message        : 10.5px / 700
 *
 * SPACING (8px grid)
 * ─────────────────────────────────────────────────────────────────
 *   XS   :  4px          (tight inline spacing)
 *   SM   :  8px          (between related elements)
 *   MD   : 16px          (form field gaps)
 *   LG   : 24px          (card padding, section breaks)
 *   XL   : 32px          (page margins)
 *
 * BORDER RADIUS
 * ─────────────────────────────────────────────────────────────────
 *   Small (badges)       : 14px
 *   Medium (inputs)      : 12px
 *   Large (buttons)      : 20px
 *   XL (cards, dialogs)  : 24px
 *   Round (pills, FAB)   : 32px
 *
 * ANIMATION CONSTANTS
 * ─────────────────────────────────────────────────────────────────
 *   Open:  scale 0.82→1.0 (320ms), fade 0→1 (250ms)
 *   Close: fade 1→0 (200ms), scale 1.0→0.84 (200ms)
 *   Blur:  GaussianBlur(10)
 *   Interpolator: SPLINE(0.22, 0.61, 0.36, 1.0)
 */
public final class DesignTokens {

    private DesignTokens() {}

    // ═══════════════════════════════════════════════════════════
    //  FORM FIELD STYLES (used by FormValidator, ValidationUtil,
    //  DialogStyler, and any programmatic form builder)
    // ═══════════════════════════════════════════════════════════

    /** Normal state for text fields inside dark-glass dialogs. */
    public static final String FIELD_NORMAL =
        "-fx-background-color: rgba(255,255,255,0.07);" +
        "-fx-text-fill: white;" +
        "-fx-border-color: rgba(139,92,246,0.30);" +
        "-fx-border-radius: 10;" +
        "-fx-background-radius: 10;" +
        "-fx-border-width: 1.5;" +
        "-fx-prompt-text-fill: rgba(167,139,250,0.42);" +
        "-fx-font-size: 13px;" +
        "-fx-padding: 10 14 10 14;";

    /** Error state for text fields — red border on dark background. */
    public static final String FIELD_ERROR =
        "-fx-background-color: rgba(239,68,68,0.10);" +
        "-fx-text-fill: white;" +
        "-fx-border-color: #f87171;" +
        "-fx-border-radius: 10;" +
        "-fx-background-radius: 10;" +
        "-fx-border-width: 2;" +
        "-fx-prompt-text-fill: rgba(239,68,68,0.45);" +
        "-fx-font-size: 13px;" +
        "-fx-padding: 10 14 10 14;";

    /** Style for error labels shown below invalid fields. */
    public static final String ERROR_LABEL =
        "-fx-text-fill: #ef4444;" +
        "-fx-font-size: 10.5px;" +
        "-fx-font-weight: 700;" +
        "-fx-padding: 2 0 0 2;";

    /** Normal state for DatePicker / ComboBox inside dark-glass dialogs. */
    public static final String COMBO_NORMAL =
        "-fx-background-color: rgba(255,255,255,0.07);" +
        "-fx-border-color: rgba(139,92,246,0.30);" +
        "-fx-border-radius: 10;" +
        "-fx-background-radius: 10;" +
        "-fx-border-width: 1.5;" +
        "-fx-font-size: 13px;" +
        "-fx-padding: 3 0 3 6;";

    /** Error state for DatePicker / ComboBox. */
    public static final String COMBO_ERROR =
        "-fx-background-color: rgba(239,68,68,0.10);" +
        "-fx-border-color: #f87171;" +
        "-fx-border-radius: 10;" +
        "-fx-background-radius: 10;" +
        "-fx-border-width: 2;" +
        "-fx-font-size: 13px;" +
        "-fx-padding: 3 0 3 6;";

    // ═══════════════════════════════════════════════════════════
    //  FORM LABEL STYLES
    // ═══════════════════════════════════════════════════════════

    /** Standard form field label — light text on dark background. */
    public static final String FIELD_LABEL =
        "-fx-text-fill: rgba(233,213,255,0.85);" +
        "-fx-font-size: 11px;" +
        "-fx-font-weight: bold;";

    /** Section divider label inside form dialogs. */
    public static final String SECTION_LABEL =
        "-fx-text-fill: rgba(216,180,254,0.55);" +
        "-fx-font-size: 10px;" +
        "-fx-font-weight: bold;" +
        "-fx-padding: 14 0 4 2;" +
        "-fx-border-color: transparent transparent rgba(255,255,255,0.12) transparent;" +
        "-fx-border-width: 0 0 1 0;" +
        "-fx-pref-width: 10000;";

    // ═══════════════════════════════════════════════════════════
    //  DIALOG PANE STYLES (AlertUtil + DialogStyler shared skin)
    // ═══════════════════════════════════════════════════════════

    /** Dark glass gradient for the outer DialogPane. */
    public static final String DIALOG_PANE =
        "-fx-background-color: linear-gradient(to bottom right," +
        "  rgba(83,31,167,0.96) 0%," +
        "  rgba(91,20,180,0.98) 45%," +
        "  rgba(109,40,217,0.94) 100%);" +
        "-fx-background-radius: 22;" +
        "-fx-border-color: rgba(255,255,255,0.28);" +
        "-fx-border-radius: 22;" +
        "-fx-border-width: 1.5;" +
        "-fx-effect: dropshadow(gaussian,rgba(109,40,217,0.70),60,0.14,0,18);";

    /** Transparent content region inside the dialog pane. */
    public static final String DIALOG_CONTENT =
        "-fx-background-color: transparent; -fx-padding: 10 16 10 16;";

    /** Transparent graphic container. */
    public static final String DIALOG_GRAPHIC =
        "-fx-background-color: transparent;";

    /** Content label (body text) inside dark dialog. */
    public static final String DIALOG_CONTENT_LABEL =
        "-fx-text-fill: rgba(220,210,255,0.95);" +
        "-fx-font-size: 13px;" +
        "-fx-padding: 4 0 4 0;";

    /** Button bar footer of the dialog. */
    public static final String DIALOG_BUTTON_BAR =
        "-fx-background-color: rgba(0,0,0,0.18);" +
        "-fx-background-radius: 0 0 22 22;" +
        "-fx-padding: 16 22 20 22;";

    /** Header panel label — white bold title. */
    public static final String DIALOG_HEADER_LABEL =
        "-fx-text-fill: white;" +
        "-fx-font-weight: 800;" +
        "-fx-font-size: 15px;";

    /** Save / OK button — purple-pink gradient. */
    public static final String BTN_SAVE =
        "-fx-background-color: linear-gradient(to right,#7c3aed,#a855f7,#c026d3);" +
        "-fx-text-fill: white;" +
        "-fx-font-size: 13px;" +
        "-fx-font-weight: bold;" +
        "-fx-background-radius: 20;" +
        "-fx-border-color: transparent;" +
        "-fx-padding: 10 28 10 28;" +
        "-fx-cursor: hand;" +
        "-fx-effect: dropshadow(gaussian,rgba(192,38,211,0.65),18,0.12,0,5);";

    /** Cancel / Close button — ghost translucent. */
    public static final String BTN_CANCEL =
        "-fx-background-color: rgba(255,255,255,0.12);" +
        "-fx-border-color: rgba(255,255,255,0.35);" +
        "-fx-border-width: 1.5;" +
        "-fx-border-radius: 20;" +
        "-fx-background-radius: 20;" +
        "-fx-text-fill: rgba(255,255,255,0.85);" +
        "-fx-font-size: 13px;" +
        "-fx-font-weight: bold;" +
        "-fx-padding: 10 26 10 26;" +
        "-fx-cursor: hand;";

    /** Builds the header panel gradient style for a given color range. */
    public static String headerPanelStyle(String colorFrom, String colorTo) {
        return "-fx-background-color: linear-gradient(to right," +
                colorFrom + "," + colorTo + ");" +
                "-fx-background-radius: 22 22 0 0;" +
                "-fx-padding: 18 22 18 22;";
    }

    // ═══════════════════════════════════════════════════════════
    //  ANIMATION CONSTANTS (used by all popup controllers)
    // ═══════════════════════════════════════════════════════════

    /** Scale factor at the start of the open animation. */
    public static final double ANIM_OPEN_SCALE = 0.82;

    /** Duration of the fade-in during open animation (ms). */
    public static final int ANIM_OPEN_FADE_MS = 250;

    /** Duration of the scale transition during open animation (ms). */
    public static final int ANIM_OPEN_SCALE_MS = 320;

    /** Duration of the fade-out during close animation (ms). */
    public static final int ANIM_CLOSE_FADE_MS = 200;

    /** Scale factor at the end of the close animation. */
    public static final double ANIM_CLOSE_SCALE = 0.84;

    /** Duration of the scale-down during close animation (ms). */
    public static final int ANIM_CLOSE_SCALE_MS = 200;

    /** Gaussian blur radius for background when popup is shown. */
    public static final double BLUR_RADIUS = 10;

    /** Spline interpolator control points for spring-like easing. */
    public static final double[] SPRING_SPLINE = { 0.22, 0.61, 0.36, 1.0 };

    // ═══════════════════════════════════════════════════════════
    //  LIGHT-THEME VARIANTS
    //  (used by theme-aware accessor methods below)
    // ═══════════════════════════════════════════════════════════

    private static final String FIELD_NORMAL_LIGHT =
        "-fx-background-color: rgba(245,240,255,0.80);" +
        "-fx-text-fill: #1e1b4b;" +
        "-fx-border-color: rgba(196,181,253,0.55);" +
        "-fx-border-radius: 10;" +
        "-fx-background-radius: 10;" +
        "-fx-border-width: 1.5;" +
        "-fx-prompt-text-fill: #a78bfa;" +
        "-fx-font-size: 13px;" +
        "-fx-padding: 10 14 10 14;";

    private static final String FIELD_ERROR_LIGHT =
        "-fx-background-color: rgba(254,226,226,0.80);" +
        "-fx-text-fill: #1e1b4b;" +
        "-fx-border-color: #f87171;" +
        "-fx-border-radius: 10;" +
        "-fx-background-radius: 10;" +
        "-fx-border-width: 2;" +
        "-fx-prompt-text-fill: rgba(239,68,68,0.55);" +
        "-fx-font-size: 13px;" +
        "-fx-padding: 10 14 10 14;";

    private static final String COMBO_NORMAL_LIGHT =
        "-fx-background-color: rgba(245,240,255,0.80);" +
        "-fx-border-color: rgba(196,181,253,0.55);" +
        "-fx-border-radius: 10;" +
        "-fx-background-radius: 10;" +
        "-fx-border-width: 1.5;" +
        "-fx-font-size: 13px;" +
        "-fx-padding: 3 0 3 6;";

    private static final String COMBO_ERROR_LIGHT =
        "-fx-background-color: rgba(254,226,226,0.80);" +
        "-fx-border-color: #f87171;" +
        "-fx-border-radius: 10;" +
        "-fx-background-radius: 10;" +
        "-fx-border-width: 2;" +
        "-fx-font-size: 13px;" +
        "-fx-padding: 3 0 3 6;";

    private static final String FIELD_LABEL_LIGHT =
        "-fx-text-fill: #4c1d95;" +
        "-fx-font-size: 11px;" +
        "-fx-font-weight: bold;";

    private static final String SECTION_LABEL_LIGHT =
        "-fx-text-fill: rgba(109,40,217,0.55);" +
        "-fx-font-size: 10px;" +
        "-fx-font-weight: bold;" +
        "-fx-padding: 14 0 4 2;" +
        "-fx-border-color: transparent transparent rgba(196,181,253,0.30) transparent;" +
        "-fx-border-width: 0 0 1 0;" +
        "-fx-pref-width: 10000;";

    private static final String DIALOG_PANE_LIGHT =
        "-fx-background-color: white;" +
        "-fx-background-radius: 22;" +
        "-fx-border-color: rgba(196,181,253,0.40);" +
        "-fx-border-radius: 22;" +
        "-fx-border-width: 1.5;" +
        "-fx-effect: dropshadow(gaussian,rgba(91,33,182,0.22),60,0.08,0,18);";

    private static final String DIALOG_CONTENT_LABEL_LIGHT =
        "-fx-text-fill: #374151;" +
        "-fx-font-size: 13px;" +
        "-fx-padding: 4 0 4 0;";

    private static final String DIALOG_BUTTON_BAR_LIGHT =
        "-fx-background-color: rgba(237,233,254,0.50);" +
        "-fx-background-radius: 0 0 22 22;" +
        "-fx-padding: 16 22 20 22;";

    private static final String BTN_CANCEL_LIGHT =
        "-fx-background-color: rgba(139,92,246,0.08);" +
        "-fx-border-color: rgba(196,181,253,0.55);" +
        "-fx-border-width: 1.5;" +
        "-fx-border-radius: 20;" +
        "-fx-background-radius: 20;" +
        "-fx-text-fill: #4c1d95;" +
        "-fx-font-size: 13px;" +
        "-fx-font-weight: bold;" +
        "-fx-padding: 10 26 10 26;" +
        "-fx-cursor: hand;";

    // ═══════════════════════════════════════════════════════════
    //  THEME-AWARE ACCESSORS
    //  Call these instead of using the constants directly when
    //  the code runs at user-interaction time (dialogs, forms).
    // ═══════════════════════════════════════════════════════════

    private static boolean dark() { return ThemeManager.getInstance().isDark(); }

    public static String fieldNormal()       { return dark() ? FIELD_NORMAL       : FIELD_NORMAL_LIGHT; }
    public static String fieldError()        { return dark() ? FIELD_ERROR        : FIELD_ERROR_LIGHT; }
    public static String comboNormal()       { return dark() ? COMBO_NORMAL       : COMBO_NORMAL_LIGHT; }
    public static String comboError()        { return dark() ? COMBO_ERROR        : COMBO_ERROR_LIGHT; }
    public static String fieldLabel()        { return dark() ? FIELD_LABEL        : FIELD_LABEL_LIGHT; }
    public static String sectionLabel()      { return dark() ? SECTION_LABEL      : SECTION_LABEL_LIGHT; }
    public static String errorLabel()        { return ERROR_LABEL; /* same in both themes */ }
    public static String dialogPane()        { return dark() ? DIALOG_PANE        : DIALOG_PANE_LIGHT; }
    public static String dialogContent()     { return DIALOG_CONTENT; /* transparent — same */ }
    public static String dialogGraphic()     { return DIALOG_GRAPHIC; /* transparent — same */ }
    public static String dialogContentLabel(){ return dark() ? DIALOG_CONTENT_LABEL : DIALOG_CONTENT_LABEL_LIGHT; }
    public static String dialogButtonBar()   { return dark() ? DIALOG_BUTTON_BAR  : DIALOG_BUTTON_BAR_LIGHT; }
    public static String dialogHeaderLabel() { return DIALOG_HEADER_LABEL; /* white on gradient — same */ }
    public static String btnSave()           { return BTN_SAVE; /* gradient — same both themes */ }
    public static String btnCancel()         { return dark() ? BTN_CANCEL         : BTN_CANCEL_LIGHT; }
}

