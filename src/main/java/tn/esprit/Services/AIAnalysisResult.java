package tn.esprit.Services;

import java.util.Collections;
import java.util.List;

/**
 * AIAnalysisResult — Immutable structured result returned by {@link AIAnalyzerService}.
 *
 * Holds the four semantic sections extracted from the AI response:
 *   • strengths            – what the plan does well
 *   • weaknesses           – problem areas identified
 *   • riskLevel            – HIGH / MEDIUM / LOW (normalised string)
 *   • suggestions          – actionable improvement advice
 *
 * Also keeps the raw AI response for debugging / full-text display.
 *
 * Created exclusively by {@link AIAnalysisResult.Builder}.
 */
public final class AIAnalysisResult {

    // ── Risk level constants ──────────────────────────────────
    public static final String RISK_HIGH   = "HIGH";
    public static final String RISK_MEDIUM = "MEDIUM";
    public static final String RISK_LOW    = "LOW";

    // ── Fields ────────────────────────────────────────────────
    private final List<String> strengths;
    private final List<String> weaknesses;
    private final String       riskLevel;    // HIGH / MEDIUM / LOW
    private final List<String> suggestions;
    private final String       rawResponse;  // full text from API

    // ── Private constructor (use Builder) ─────────────────────
    private AIAnalysisResult(Builder b) {
        this.strengths   = Collections.unmodifiableList(b.strengths);
        this.weaknesses  = Collections.unmodifiableList(b.weaknesses);
        this.riskLevel   = b.riskLevel;
        this.suggestions = Collections.unmodifiableList(b.suggestions);
        this.rawResponse = b.rawResponse;
    }

    // ── Getters ───────────────────────────────────────────────

    /** List of strength bullet points. Never null; may be empty. */
    public List<String> getStrengths()   { return strengths; }

    /** List of weakness bullet points. Never null; may be empty. */
    public List<String> getWeaknesses()  { return weaknesses; }

    /**
     * Normalised risk level: "HIGH", "MEDIUM", or "LOW".
     * Returns "UNKNOWN" if the AI did not provide one.
     */
    public String getRiskLevel()         { return riskLevel; }

    /** List of improvement suggestion bullet points. Never null; may be empty. */
    public List<String> getSuggestions() { return suggestions; }

    /** Full raw text returned by the AI API. */
    public String getRawResponse()       { return rawResponse; }

    // ── Derived helpers ───────────────────────────────────────

    /**
     * Hex color for the risk level, matching the project design system.
     *   LOW    → #16a34a (green)
     *   MEDIUM → #ea580c (orange)
     *   HIGH   → #dc2626 (red)
     */
    public String riskHexColor() {
        return switch (riskLevel) {
            case RISK_LOW    -> "#16a34a";
            case RISK_HIGH   -> "#dc2626";
            default          -> "#ea580c"; // MEDIUM or UNKNOWN
        };
    }

    /** Emoji icon for the risk level. */
    public String riskIcon() {
        return switch (riskLevel) {
            case RISK_LOW    -> "🟢";
            case RISK_HIGH   -> "🔴";
            default          -> "🟠";
        };
    }

    /** Formats strengths as a single multiline string for Labels. */
    public String strengthsText() {
        return formatBullets(strengths, "No strengths identified.");
    }

    /** Formats weaknesses as a single multiline string for Labels. */
    public String weaknessesText() {
        return formatBullets(weaknesses, "No weaknesses identified.");
    }

    /** Formats suggestions as a single multiline string for Labels. */
    public String suggestionsText() {
        return formatBullets(suggestions, "No suggestions provided.");
    }

    private String formatBullets(List<String> items, String fallback) {
        if (items.isEmpty()) return fallback;
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append("• ").append(item.trim()).append("\n");
        }
        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return "AIAnalysisResult{riskLevel=" + riskLevel
                + ", strengths=" + strengths.size()
                + ", weaknesses=" + weaknesses.size()
                + ", suggestions=" + suggestions.size() + "}";
    }

    // ─────────────────────────────────────────────────────────
    // Builder
    // ─────────────────────────────────────────────────────────

    public static final class Builder {

        private List<String> strengths   = Collections.emptyList();
        private List<String> weaknesses  = Collections.emptyList();
        private String       riskLevel   = "UNKNOWN";
        private List<String> suggestions = Collections.emptyList();
        private String       rawResponse = "";

        public Builder strengths(List<String> v)   { this.strengths   = v != null ? v : Collections.emptyList(); return this; }
        public Builder weaknesses(List<String> v)  { this.weaknesses  = v != null ? v : Collections.emptyList(); return this; }
        public Builder riskLevel(String v)         { this.riskLevel   = v != null ? v.trim().toUpperCase() : "UNKNOWN"; return this; }
        public Builder suggestions(List<String> v) { this.suggestions = v != null ? v : Collections.emptyList(); return this; }
        public Builder rawResponse(String v)       { this.rawResponse = v != null ? v : ""; return this; }

        public AIAnalysisResult build() { return new AIAnalysisResult(this); }
    }
}

