package tn.esprit.Services;

/**
 * Immutable value object returned by {@link FundingSimulator#simulate}.
 *
 * Contains:
 *   - burnRate        monthly cash consumption (expenses − revenue)
 *   - runwayMonths    how many months the funding will last (Double.MAX_VALUE = infinite)
 *   - status          SAFE / MEDIUM / CRITICAL
 *   - summary         human-readable one-line summary for display
 */
public final class SimulationResult {

    /** The three possible financial health statuses. */
    public enum Status { SAFE, MEDIUM, CRITICAL }

    // ── Fields ────────────────────────────────────────────────
    private final double requestedFunding;
    private final double monthlyExpenses;
    private final double expectedMonthlyRevenue;
    private final double burnRate;
    private final double runwayMonths;
    private final Status status;
    private final String summary;

    // ── Package-private constructor (created by FundingSimulator only) ──

    SimulationResult(double requestedFunding,
                     double monthlyExpenses,
                     double expectedMonthlyRevenue,
                     double burnRate,
                     double runwayMonths,
                     Status status,
                     String summary) {
        this.requestedFunding        = requestedFunding;
        this.monthlyExpenses         = monthlyExpenses;
        this.expectedMonthlyRevenue  = expectedMonthlyRevenue;
        this.burnRate                = burnRate;
        this.runwayMonths            = runwayMonths;
        this.status                  = status;
        this.summary                 = summary;
    }

    // ── Getters ───────────────────────────────────────────────

    public double getRequestedFunding()       { return requestedFunding; }
    public double getMonthlyExpenses()        { return monthlyExpenses; }
    public double getExpectedMonthlyRevenue() { return expectedMonthlyRevenue; }
    public double getBurnRate()               { return burnRate; }
    public double getRunwayMonths()           { return runwayMonths; }
    public Status getStatus()                 { return status; }
    public String getSummary()                { return summary; }

    /** Returns true when the startup is revenue-positive (infinite runway). */
    public boolean isInfiniteRunway() {
        return runwayMonths == Double.MAX_VALUE;
    }

    /**
     * Hex color string for the status (matches design system).
     *   SAFE     → green  #16a34a
     *   MEDIUM   → orange #ea580c
     *   CRITICAL → red    #dc2626
     */
    public String statusHexColor() {
        return switch (status) {
            case SAFE     -> "#16a34a";
            case MEDIUM   -> "#ea580c";
            case CRITICAL -> "#dc2626";
        };
    }

    /** CSS style class for the status label. */
    public String statusStyleClass() {
        return switch (status) {
            case SAFE     -> "sim-status-safe";
            case MEDIUM   -> "sim-status-medium";
            case CRITICAL -> "sim-status-critical";
        };
    }

    /** Emoji icon representing the health status. */
    public String statusIcon() {
        return switch (status) {
            case SAFE     -> "✅";
            case MEDIUM   -> "⚠️";
            case CRITICAL -> "🚨";
        };
    }

    @Override
    public String toString() {
        return "SimulationResult{burnRate=" + burnRate
                + ", runwayMonths=" + (isInfiniteRunway() ? "∞" : String.format("%.1f", runwayMonths))
                + ", status=" + status + "}";
    }
}

