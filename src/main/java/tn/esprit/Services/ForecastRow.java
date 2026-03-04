package tn.esprit.Services;

/**
 * Immutable data model for a single month's row in a financial forecast.
 *
 * Fields:
 *   month            — 1-based month number (1–12)
 *   revenue          — projected revenue for this month (TND)
 *   expenses         — fixed monthly expenses (TND)
 *   netProfit        — revenue − expenses for this month (TND, may be negative)
 *   cumulativeProfit — running sum of netProfit from month 1 to this month (TND)
 *
 * Produced by {@link GeminiForecastService#computeForecastRows(double, double, double)}.
 * Consumed by {@link tn.esprit.utils.ForecastExportUtil} for PDF and Excel export.
 */
public class ForecastRow {

    private final int    month;
    private final double revenue;
    private final double expenses;
    private final double netProfit;
    private final double cumulativeProfit;

    public ForecastRow(int    month,
                       double revenue,
                       double expenses,
                       double netProfit,
                       double cumulativeProfit) {
        this.month            = month;
        this.revenue          = revenue;
        this.expenses         = expenses;
        this.netProfit        = netProfit;
        this.cumulativeProfit = cumulativeProfit;
    }

    // ── Getters ───────────────────────────────────────────────

    public int    getMonth()            { return month;            }
    public double getRevenue()          { return revenue;          }
    public double getExpenses()         { return expenses;         }
    public double getNetProfit()        { return netProfit;        }
    public double getCumulativeProfit() { return cumulativeProfit; }

    @Override
    public String toString() {
        return String.format("ForecastRow{month=%d, revenue=%.2f, expenses=%.2f, " +
                             "netProfit=%.2f, cumulative=%.2f}",
                month, revenue, expenses, netProfit, cumulativeProfit);
    }
}

