package tn.esprit.Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * GeminiForecastService — Generates a 12-month financial forecast
 * and investment recommendation via the Google Gemini API.
 *
 * API endpoint: POST https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent
 * Authentication: API key as query parameter.
 *
 * The HTTP call is BLOCKING — always invoke from a background thread
 * (e.g. a JavaFX {@code Task<String>}).
 *
 * Falls back to a mathematically accurate locally-computed forecast
 * when the API quota is exceeded or unavailable.
 */
public class GeminiForecastService {

    // ── API constants ─────────────────────────────────────────
    private static final String  API_BASE_URL    =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final int     TIMEOUT_SECONDS = 30;
    private static final double  TEMPERATURE     = 0.3; // low for deterministic numeric output
    private static final int     MAX_RETRIES     = 2;
    private static final long[]  RETRY_DELAYS_MS = {3_000L, 8_000L};

    // ── Hardcoded API key ─────────────────────────────────────
    private static final String  HARDCODED_KEY   = "AIzaSyAyXNEfA-TtdsFqT52Lh7PLO82CK3EUdEg";

    // ── State ─────────────────────────────────────────────────
    private final String     apiKey;
    private final HttpClient httpClient;

    // ─────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────

    /** Default constructor — uses the hardcoded Gemini key. */
    public GeminiForecastService() {
        this(HARDCODED_KEY);
    }

    /** Allows injecting a different key for testing. */
    public GeminiForecastService(String apiKey) {
        this.apiKey     = apiKey != null ? apiKey.trim() : "";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────

    /**
     * Generates a 12-month financial forecast and investment recommendation.
     *
     * Tries the Gemini API first; falls back to local computation on any failure.
     *
     * @param revenue    current monthly revenue  (must be >= 0)
     * @param growthRate expected monthly growth rate in percent (must be >= 0)
     * @param expenses   fixed monthly expenses (must be >= 0)
     * @return formatted multi-line forecast string, never null
     */
    public String generateForecast(double revenue, double growthRate, double expenses) {
        // ── Guard: key missing → go straight to local ────────
        if (apiKey.isEmpty()) {
            return buildLocalForecast(revenue, growthRate, expenses);
        }

        // ── Build request ─────────────────────────────────────
        String prompt  = buildPrompt(revenue, growthRate, expenses);
        String payload = buildJsonPayload(prompt);
        String url     = API_BASE_URL + "?key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        // ── Send with retry on 429 ─────────────────────────────
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            HttpResponse<String> response;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException ex) {
                System.err.println("[GeminiForecastService] Network error, using local: " + ex.getMessage());
                return buildLocalForecast(revenue, growthRate, expenses);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return buildLocalForecast(revenue, growthRate, expenses);
            }

            // Rate-limit — wait and retry
            if (response.statusCode() == 429) {
                if (attempt < MAX_RETRIES) {
                    System.out.printf("[GeminiForecastService] 429 — retrying in %ds (attempt %d/%d)%n",
                            RETRY_DELAYS_MS[attempt] / 1000, attempt + 1, MAX_RETRIES);
                    try { Thread.sleep(RETRY_DELAYS_MS[attempt]); }
                    catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    continue;
                }
                // All retries exhausted
                System.err.println("[GeminiForecastService] Quota exceeded, using local forecast.");
                return buildLocalForecast(revenue, growthRate, expenses);
            }

            // Non-200 errors → fallback
            if (response.statusCode() != 200) {
                System.err.println("[GeminiForecastService] HTTP " + response.statusCode() + " — using local.");
                return buildLocalForecast(revenue, growthRate, expenses);
            }

            // ── Success — parse and return ────────────────────
            String text = extractText(response.body());
            if (text != null && !text.isBlank()) {
                return text;
            }
            // Unparseable body → local fallback
            return buildLocalForecast(revenue, growthRate, expenses);
        }

        return buildLocalForecast(revenue, growthRate, expenses);
    }

    // ─────────────────────────────────────────────────────────
    // Prompt engineering
    // ─────────────────────────────────────────────────────────

    /**
     * Builds a precise financial analyst prompt.
     * Uses strict section labels so the output is predictable.
     */
    private String buildPrompt(double revenue, double growthRate, double expenses) {
        return String.format(
            "You are a professional startup financial analyst.\n\n" +
            "Financial inputs:\n" +
            "  - Current Monthly Revenue:       $%,.2f\n" +
            "  - Expected Monthly Growth Rate:  %.2f%%\n" +
            "  - Fixed Monthly Expenses:        $%,.2f\n\n" +
            "Generate EXACTLY the following two sections — no preamble, no extra text:\n\n" +
            "SECTION 1 — 12-MONTH FORECAST TABLE\n" +
            "Use this plain-text format (one row per month, space-aligned):\n\n" +
            "Month | Revenue       | Profit/Loss\n" +
            "------+---------------+--------------\n" +
            "1     | $X,XXX.XX     | $X,XXX.XX\n" +
            "12    | $X,XXX.XX     | $X,XXX.XX\n\n" +
            "SECTION 2 — INVESTMENT RECOMMENDATION\n" +
            "Write exactly one of: INVEST | CAUTION | HIGH RISK\n" +
            "Then 2-3 sentences of justification based on the numbers.\n",
            revenue, growthRate, expenses
        );
    }

    // ─────────────────────────────────────────────────────────
    // JSON helpers
    // ─────────────────────────────────────────────────────────

    /** Wraps the prompt in the Gemini generateContent JSON envelope. */
    private String buildJsonPayload(String prompt) {
        String escaped = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
        return "{\"contents\":[{\"parts\":[{\"text\":\"" + escaped + "\"}]}],"
             + "\"generationConfig\":{\"temperature\":" + TEMPERATURE + "}}";
    }

    /**
     * Extracts the 'text' field from the Gemini JSON response.
     * Gemini shape: { "candidates":[{"content":{"parts":[{"text":"..."}]}}] }
     */
    private String extractText(String body) {
        if (body == null || body.isBlank()) return null;
        final String key = "\"text\":";
        int ci = body.indexOf(key);
        if (ci == -1) return null;
        int start = body.indexOf('"', ci + key.length()) + 1;
        int end   = findStringEnd(body, start);
        if (start <= 0 || end <= start) return null;
        return unescape(body.substring(start, end));
    }

    /** Scans forward past escaped characters to find the closing quote. */
    private int findStringEnd(String s, int from) {
        for (int i = from; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') { i++; continue; } // skip escaped char
            if (c == '"')  return i;
        }
        return -1;
    }

    /** Unescapes common JSON string escapes. */
    private String unescape(String s) {
        return s.replace("\\n", "\n")
                .replace("\\r", "")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    // ─────────────────────────────────────────────────────────
    // Structured forecast rows (for export)
    // ─────────────────────────────────────────────────────────

    /**
     * Computes 12 {@link ForecastRow} objects using the same compound-growth
     * formula as the local fallback forecast.
     *
     * Always runs locally — no API call.  Use this to populate export data
     * alongside any display forecast (Gemini-based or local text).
     *
     * @param revenue    baseline monthly revenue (TND)
     * @param growthRate expected monthly growth rate in percent
     * @param expenses   fixed monthly expenses (TND)
     * @return immutable list of 12 forecast rows in month order
     */
    public static List<ForecastRow> computeForecastRows(
            double revenue, double growthRate, double expenses) {

        List<ForecastRow> rows = new ArrayList<>(12);
        double rate       = growthRate / 100.0;
        double cumulative = 0;

        for (int month = 1; month <= 12; month++) {
            // Revenue compounds each month: R(n) = R0 × (1 + r)^(n-1)
            double projRevenue = revenue * Math.pow(1 + rate, month - 1);
            double netProfit   = projRevenue - expenses;
            cumulative        += netProfit;
            rows.add(new ForecastRow(month, projRevenue, expenses, netProfit, cumulative));
        }
        return rows;
    }

    // ─────────────────────────────────────────────────────────
    // Local fallback forecast
    // ─────────────────────────────────────────────────────────

    /**
     * Computes a mathematically accurate 12-month forecast entirely locally.
     * Revenue compounds at the given growth rate each month.
     * Used when the Gemini API is unavailable or quota is exhausted.
     */
    private String buildLocalForecast(double revenue, double growthRate, double expenses) {
        double rate = growthRate / 100.0;

        StringBuilder sb = new StringBuilder();
        sb.append("⚠  Locally computed forecast (AI API quota exceeded)\n");
        sb.append("━".repeat(52)).append("\n\n");

        // ── Table header ──────────────────────────────────────
        sb.append(String.format("%-7s  %-16s  %-16s%n", "Month", "Revenue", "Profit / Loss"));
        sb.append("─".repeat(48)).append("\n");

        double totalProfit = 0;
        double finalRevenue = revenue;

        for (int month = 1; month <= 12; month++) {
            // Revenue compounds each month: R(n) = R0 × (1 + r)^(n-1)
            double projRevenue  = revenue * Math.pow(1 + rate, month - 1);
            double profitLoss   = projRevenue - expenses;
            totalProfit        += profitLoss;
            if (month == 12) finalRevenue = projRevenue;

            String plSign = profitLoss >= 0 ? "+" : "";
            // Pre-format numbers with comma grouping, then pad as strings — avoids
            // the illegal "%-15,.2f" combination (comma flag must precede width in Java).
            String revStr = String.format("%,.2f", projRevenue);
            String plStr  = plSign + String.format("%,.2f", Math.abs(profitLoss));
            sb.append(String.format("%-7d  $%-16s  %-16s%n", month, revStr, plStr));
        }

        sb.append("─".repeat(48)).append("\n");
        String totalStr = (totalProfit >= 0 ? "+" : "") + String.format("%,.2f", Math.abs(totalProfit));
        sb.append(String.format("%-7s  %-17s  %-16s%n", "TOTAL", "", totalStr)).append("\n");

        // ── Investment recommendation ─────────────────────────
        sb.append("━".repeat(52)).append("\n");
        sb.append("INVESTMENT RECOMMENDATION\n");
        sb.append("─".repeat(48)).append("\n");

        double avgMonthlyProfit = totalProfit / 12.0;

        if (avgMonthlyProfit > 0 && finalRevenue > expenses * 1.2) {
            sb.append("✅  INVEST\n\n");
            sb.append("Revenue growth consistently outpaces monthly expenses. ");
            sb.append("The business demonstrates a healthy positive cash flow trajectory ");
            sb.append("with a final monthly revenue of $").append(String.format("%,.0f", finalRevenue)).append(", ");
            sb.append("making it an attractive investment opportunity.");
        } else if (avgMonthlyProfit >= -(expenses * 0.15)) {
            sb.append("⚠  CAUTION\n\n");
            sb.append("The business is approaching break-even but carries moderate risk. ");
            sb.append("Average monthly cash flow is $").append(String.format("%,.0f", avgMonthlyProfit)).append(". ");
            sb.append("Invest cautiously and set clear revenue milestones before committing full capital.");
        } else {
            sb.append("🔴  HIGH RISK\n\n");
            sb.append("Projected expenses significantly exceed revenue throughout the forecast period. ");
            sb.append("Average monthly loss: $").append(String.format("%,.0f", Math.abs(avgMonthlyProfit))).append(". ");
            sb.append("Substantial revenue growth or cost reduction is required before investment is advisable.");
        }

        return sb.toString();
    }
}

