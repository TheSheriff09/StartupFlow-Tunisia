package tn.esprit.Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CurrencyService — converts monetary amounts between currencies
 * using the ExchangeRate-API free tier (no API key required).
 *
 * Endpoint: GET https://open.er-api.com/v6/latest/{FROM}
 *
 * Response shape:
 * {
 *   "result": "success",
 *   "base_code": "TND",
 *   "rates": { "USD": 0.3239, "EUR": 0.2987, ... }
 * }
 *
 * The HTTP call is BLOCKING — always invoke from a background thread
 * (e.g. a JavaFX {@code Task<Map<String,Double>>}).
 */
public class CurrencyService {

    // ── API constants ─────────────────────────────────────────
    private static final String API_BASE  = "https://open.er-api.com/v6/latest/";
    private static final int    TIMEOUT_S = 15;

    // ── Shared HttpClient (thread-safe, reusable) ─────────────
    private final HttpClient httpClient;

    public CurrencyService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_S))
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────

    /**
     * Converts {@code amount} from {@code from} currency to {@code to} currency.
     *
     * @param from   source currency code, e.g. "TND"
     * @param to     target currency code, e.g. "USD"
     * @param amount the amount to convert (must be > 0)
     * @return converted amount
     * @throws CurrencyException on network errors, bad responses, or unknown currency
     */
    public double convertCurrency(String from, String to, double amount) throws CurrencyException {
        validateInputs(from, to, amount);

        String body = fetchRates(from.toUpperCase());
        double rate  = extractRate(body, to.toUpperCase());
        return amount * rate;
    }

    /**
     * Converts {@code amount} from {@code from} into multiple target currencies at once.
     * Returns a LinkedHashMap preserving insertion order.
     *
     * @param from    source currency code
     * @param amount  amount to convert
     * @param targets list of target currency codes, e.g. "USD", "EUR"
     * @return map of currency code → converted amount
     * @throws CurrencyException on any error
     */
    public Map<String, Double> convertToMultiple(String from, double amount,
                                                  String... targets) throws CurrencyException {
        if (targets == null || targets.length == 0) {
            throw new CurrencyException("At least one target currency must be specified.");
        }
        validateInputs(from, targets[0], amount);

        // Fetch rates once for the source currency
        String body = fetchRates(from.toUpperCase());

        Map<String, Double> results = new LinkedHashMap<>();
        for (String to : targets) {
            double rate = extractRate(body, to.toUpperCase());
            results.put(to.toUpperCase(), amount * rate);
        }
        return results;
    }

    // ─────────────────────────────────────────────────────────
    // Input validation
    // ─────────────────────────────────────────────────────────

    private void validateInputs(String from, String to, double amount) throws CurrencyException {
        if (from == null || from.isBlank())
            throw new CurrencyException("Source currency code must not be empty.");
        if (to == null || to.isBlank())
            throw new CurrencyException("Target currency code must not be empty.");
        if (amount <= 0)
            throw new CurrencyException("Amount must be greater than zero.");
    }

    // ─────────────────────────────────────────────────────────
    // HTTP
    // ─────────────────────────────────────────────────────────

    /** Fetches the raw JSON rates for the given base currency. */
    private String fetchRates(String baseCurrency) throws CurrencyException {
        String url = API_BASE + baseCurrency;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(TIMEOUT_S))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException ex) {
            throw new CurrencyException(
                "Network error — could not reach the exchange rate API.\n" + ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CurrencyException("Request was interrupted. Please try again.", ex);
        }

        if (response.statusCode() != 200) {
            throw new CurrencyException(
                "Exchange rate API returned HTTP " + response.statusCode() + ".");
        }

        // Verify the API returned success
        String body = response.body();
        if (body.contains("\"error\"") || !body.contains("\"success\"") && body.contains("\"error\"")) {
            throw new CurrencyException("Exchange rate API error: " + truncate(body, 200));
        }

        // Check result field
        if (body.contains("\"result\":\"error\"")) {
            throw new CurrencyException("Unknown base currency: " + baseCurrency);
        }

        return body;
    }

    // ─────────────────────────────────────────────────────────
    // JSON parsing (no external dependency)
    // ─────────────────────────────────────────────────────────

    /**
     * Extracts the rate for {@code targetCurrency} from the "rates" object.
     *
     * Scans for "TARGET_CODE": NUMBER in the JSON string.
     * Handles whitespace and decimal/integer values.
     */
    private double extractRate(String body, String targetCurrency) throws CurrencyException {
        // Pattern: "USD": 0.3239   or  "USD":0.3239
        String key = "\"" + targetCurrency + "\"";
        int ki = body.indexOf(key);
        if (ki == -1) {
            throw new CurrencyException(
                "Unknown target currency: " + targetCurrency +
                ". Check that the currency code is valid (e.g. USD, EUR, GBP).");
        }

        // Skip past the key, colon, and any whitespace to reach the number
        int colon = body.indexOf(':', ki + key.length());
        if (colon == -1) throw new CurrencyException("Malformed rate for " + targetCurrency);

        int numStart = colon + 1;
        while (numStart < body.length() && body.charAt(numStart) == ' ') numStart++;

        // Read until end of number (digit, dot, or minus)
        int numEnd = numStart;
        while (numEnd < body.length()) {
            char c = body.charAt(numEnd);
            if (Character.isDigit(c) || c == '.' || c == '-' || c == 'E' || c == 'e') {
                numEnd++;
            } else {
                break;
            }
        }

        String numStr = body.substring(numStart, numEnd).trim();
        try {
            double rate = Double.parseDouble(numStr);
            if (rate <= 0) throw new CurrencyException("Invalid rate value for " + targetCurrency + ": " + rate);
            return rate;
        } catch (NumberFormatException e) {
            throw new CurrencyException("Could not parse exchange rate for " + targetCurrency + ": '" + numStr + "'");
        }
    }

    private static String truncate(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) + "…" : s;
    }

    // ─────────────────────────────────────────────────────────
    // CurrencyException — checked, surfaced cleanly in UI
    // ─────────────────────────────────────────────────────────

    /** Thrown for any error during currency conversion (network, API, validation). */
    public static class CurrencyException extends Exception {
        public CurrencyException(String message)                     { super(message); }
        public CurrencyException(String message, Throwable cause)    { super(message, cause); }
    }
}

