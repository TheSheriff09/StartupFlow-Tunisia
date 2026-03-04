package tn.esprit.Services;

import tn.esprit.entities.BusinessPlan;

import java.util.List;

/**
 * InvestmentScorer — Pure scoring logic, no DB / no UI dependencies.
 *
 * Calculates an Investment Score in the range [0, 100] for a list of
 * BusinessPlans belonging to a single startup.  The score is computed
 * per-plan and averaged, so a startup with multiple plans gets a
 * representative composite score.
 *
 * Scoring breakdown (per plan, max = 100):
 *   ─ fundingRequired (30 pts max)   lower funding → higher score
 *   ─ financialForecast (25 pts max) keyword-based quality probe
 *   ─ status           (20 pts max)  Approved +20, Pending +10, Rejected −10
 *   ─ marketAnalysis   (15 pts max)  length/quality check
 *   ─ timeline         (10 pts max)  realistic range check
 */
public final class InvestmentScorer {

    // ── Thresholds for fundingRequired scoring ────────────────
    private static final double FUNDING_LOW    =  50_000;   // ≤ this → full 30 pts
    private static final double FUNDING_MID    = 500_000;   // ≤ this → 15 pts
    private static final double FUNDING_HIGH   = 2_000_000; // ≤ this → 8 pts; above → 0 pts

    // ── Thresholds for financialForecast quality ──────────────
    private static final int    FORECAST_STRONG_WORDS = 3;  // keywords found → 25 pts
    private static final int    FORECAST_WEAK_WORDS   = 1;  // keywords found → 12 pts

    // ── Thresholds for marketAnalysis quality ─────────────────
    private static final int    ANALYSIS_STRONG = 200;      // ≥ chars → 15 pts
    private static final int    ANALYSIS_MEDIUM =  80;      // ≥ chars → 8 pts
                                                            // < 80     → 0 pts (penalty applied)

    // ── Keywords that signal a solid financial forecast ───────
    private static final String[] FORECAST_KEYWORDS = {
        "revenue", "profit", "growth", "roi", "margin",
        "cash flow", "break-even", "ebitda", "forecast", "projection"
    };

    private InvestmentScorer() { /* utility */ }

    // ─────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────

    /**
     * Returns a composite Investment Score [0–100] for a startup,
     * aggregated from all of its business plans.
     *
     * @param plans list of BusinessPlan objects (may be empty)
     * @return score in [0.0, 100.0]; returns 0.0 if no plans exist
     */
    public static double calculate(List<BusinessPlan> plans) {
        if (plans == null || plans.isEmpty()) return 0.0;

        double total = 0.0;
        for (BusinessPlan bp : plans) {
            total += scorePlan(bp);
        }
        // Average over all plans, then clamp to [0, 100]
        double avg = total / plans.size();
        return Math.min(100.0, Math.max(0.0, avg));
    }

    /**
     * Score a single BusinessPlan out of 100.
     */
    public static double scorePlan(BusinessPlan bp) {
        double score = 0.0;

        // ── (A) fundingRequired — 30 pts ─────────────────────
        score += scoreFunding(bp.getFundingRequired());

        // ── (B) financialForecast quality — 25 pts ───────────
        score += scoreForecast(bp.getFinancialForecast());

        // ── (C) status — ±20 pts ─────────────────────────────
        score += scoreStatus(bp.getStatus());

        // ── (D) marketAnalysis quality — 15 pts ──────────────
        score += scoreMarketAnalysis(bp.getMarketAnalysis());

        // ── (E) timeline realism — 10 pts ────────────────────
        score += scoreTimeline(bp.getTimeline());

        return score;
    }

    // ─────────────────────────────────────────────────────────
    // Private scoring helpers
    // ─────────────────────────────────────────────────────────

    /**
     * Lower funding required is better (signals capital-efficient idea).
     * Returns 0–30 points.
     */
    private static double scoreFunding(Double funding) {
        if (funding == null || funding <= 0) return 0;
        if (funding <= FUNDING_LOW)    return 30;
        if (funding <= FUNDING_MID)    return 15;
        if (funding <= FUNDING_HIGH)   return 8;
        return 0;
    }

    /**
     * Probe the financial forecast text for quality keywords.
     * Returns 0–25 points.
     */
    private static double scoreForecast(String forecast) {
        if (forecast == null || forecast.isBlank()) return 0;
        String lower = forecast.toLowerCase();
        int hits = 0;
        for (String kw : FORECAST_KEYWORDS) {
            if (lower.contains(kw)) hits++;
        }
        if (hits >= FORECAST_STRONG_WORDS) return 25;
        if (hits >= FORECAST_WEAK_WORDS)   return 12;
        return 5; // has text but no quality keywords
    }

    /**
     * Status gives a direct bonus/penalty.
     * Returns −10 to +20 points.
     */
    private static double scoreStatus(String status) {
        if (status == null) return 0;
        return switch (status.trim()) {
            case "Approved" -> 20;
            case "Pending"  -> 10;
            case "Rejected" -> -10;
            default         -> 0;
        };
    }

    /**
     * Longer, more detailed market analysis earns more points.
     * Returns 0–15 points (penalty applied for very short text).
     */
    private static double scoreMarketAnalysis(String analysis) {
        if (analysis == null || analysis.isBlank()) return 0;
        int len = analysis.trim().length();
        if (len >= ANALYSIS_STRONG) return 15;
        if (len >= ANALYSIS_MEDIUM) return 8;
        return 2; // present but minimal — small partial credit
    }

    /**
     * Tries to parse a realistic timeline in months from the timeline string.
     * Accepts formats like "6 months", "12", "18 months", "2 years", etc.
     * Returns 0–10 points.
     */
    private static double scoreTimeline(String timeline) {
        if (timeline == null || timeline.isBlank()) return 0;
        try {
            // Extract numeric part
            String numeric = timeline.replaceAll("[^0-9.]", "").trim();
            if (numeric.isEmpty()) return 2; // text exists but unparseable
            double value = Double.parseDouble(numeric);

            // Convert years to months if "year" appears in string
            if (timeline.toLowerCase().contains("year")) value *= 12;

            // Realistic: 3–36 months earns full points
            if (value >= 3 && value <= 36) return 10;
            if (value >= 1 && value <= 60) return 5;  // borderline
            return 0; // unrealistic (< 1 month or > 5 years)
        } catch (NumberFormatException e) {
            return 2; // non-numeric but something was written
        }
    }

    // ─────────────────────────────────────────────────────────
    // Score band helpers
    // ─────────────────────────────────────────────────────────

    /** Returns "green" / "orange" / "red" CSS color class for the score. */
    public static String colorClass(double score) {
        if (score > 75) return "score-green";
        if (score >= 50) return "score-orange";
        return "score-red";
    }

    /** Returns a hex color string for inline -fx-text-fill / -fx-background-color use. */
    public static String hexColor(double score) {
        if (score > 75) return "#16a34a"; // green-600
        if (score >= 50) return "#ea580c"; // orange-600
        return "#dc2626";                  // red-600
    }

    /** Short descriptive band label. */
    public static String band(double score) {
        if (score > 75) return "High";
        if (score >= 50) return "Medium";
        return "Low";
    }
}

