package tn.esprit.Services;

/**
 * FundingSimulator — Stateless financial runway calculator.
 *
 * Core formula:
 *   burnRate     = monthlyExpenses − expectedMonthlyRevenue
 *   runwayMonths = requestedFunding / burnRate
 *
 * Status thresholds:
 *   runwayMonths < 6   → CRITICAL (Red)
 *   runwayMonths 6–12  → MEDIUM   (Orange)
 *   runwayMonths > 12  → SAFE     (Green)
 *   burnRate ≤ 0        → Infinite runway → SAFE
 *
 * Usage:
 * <pre>
 *   FundingSimulator sim = new FundingSimulator();
 *   SimulationResult r   = sim.simulate(200_000, 30_000, 15_000);
 *   System.out.println(r.getStatus());        // MEDIUM
 *   System.out.println(r.getRunwayMonths());  // ~13.3
 * </pre>
 */
public class FundingSimulator {

    // ── Status thresholds ─────────────────────────────────────
    private static final double RUNWAY_CRITICAL_MAX =  6.0;  // months
    private static final double RUNWAY_MEDIUM_MAX   = 12.0;  // months

    /**
     * Simulates the financial runway for a startup.
     *
     * @param requestedFunding       total funding amount (must be > 0)
     * @param monthlyExpenses        total monthly costs  (must be ≥ 0)
     * @param expectedMonthlyRevenue expected monthly income (must be ≥ 0)
     * @return a {@link SimulationResult} containing all derived metrics
     * @throws IllegalArgumentException if any input is negative, or funding is zero
     */
    public SimulationResult simulate(double requestedFunding,
                                     double monthlyExpenses,
                                     double expectedMonthlyRevenue) {

        // ── Input validation ─────────────────────────────────
        if (requestedFunding <= 0)
            throw new IllegalArgumentException("Requested funding must be greater than zero.");
        if (monthlyExpenses < 0)
            throw new IllegalArgumentException("Monthly expenses cannot be negative.");
        if (expectedMonthlyRevenue < 0)
            throw new IllegalArgumentException("Expected monthly revenue cannot be negative.");

        // ── Core calculation ──────────────────────────────────
        double burnRate     = monthlyExpenses - expectedMonthlyRevenue;
        double runwayMonths;
        SimulationResult.Status status;
        String summary;

        if (burnRate <= 0) {
            // Revenue meets or exceeds expenses → startup is cash-flow positive
            runwayMonths = Double.MAX_VALUE;
            status       = SimulationResult.Status.SAFE;
            summary      = buildSummary(requestedFunding, monthlyExpenses,
                                        expectedMonthlyRevenue, burnRate, runwayMonths, status);
        } else {
            runwayMonths = requestedFunding / burnRate;
            status       = classifyStatus(runwayMonths);
            summary      = buildSummary(requestedFunding, monthlyExpenses,
                                        expectedMonthlyRevenue, burnRate, runwayMonths, status);
        }

        return new SimulationResult(
                requestedFunding,
                monthlyExpenses,
                expectedMonthlyRevenue,
                burnRate,
                runwayMonths,
                status,
                summary
        );
    }

    // ─────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────

    private SimulationResult.Status classifyStatus(double runwayMonths) {
        if (runwayMonths < RUNWAY_CRITICAL_MAX) return SimulationResult.Status.CRITICAL;
        if (runwayMonths <= RUNWAY_MEDIUM_MAX)  return SimulationResult.Status.MEDIUM;
        return SimulationResult.Status.SAFE;
    }

    private String buildSummary(double funding, double expenses, double revenue,
                                 double burnRate, double runway,
                                 SimulationResult.Status status) {
        String runwayStr = (runway == Double.MAX_VALUE)
                ? "Infinite (cash-flow positive)"
                : String.format("%.1f months", runway);

        return String.format(
                "Funding: $%,.0f  |  Burn Rate: $%,.0f/mo  |  Runway: %s  |  Status: %s",
                funding, burnRate, runwayStr, status
        );
    }
}

