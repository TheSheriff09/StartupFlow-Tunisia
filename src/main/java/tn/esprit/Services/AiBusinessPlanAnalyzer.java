package tn.esprit.Services;

import tn.esprit.entities.BusinessPlan;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * AiBusinessPlanAnalyzer — Sends a BusinessPlan to the OpenAI Chat Completions
 * API and returns the plain-text feedback.
 *
 * Configuration:
 *   Set the environment variable  OPENAI_API_KEY  before running the app,
 *   or replace the constant DEFAULT_API_KEY with your key.
 *
 * The request is a synchronous blocking HTTP call and may take 3–15 seconds.
 * Always call this from a background thread (e.g. Task&lt;String&gt;) in JavaFX.
 *
 * Usage:
 * <pre>
 *   AiBusinessPlanAnalyzer ai = new AiBusinessPlanAnalyzer();
 *   String feedback = ai.analyzeBusinessPlan(plan);
 * </pre>
 */
public class AiBusinessPlanAnalyzer {

    // ── Configuration ─────────────────────────────────────────
    private static final String API_URL     = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL       = "gpt-3.5-turbo";
    private static final int    TIMEOUT_SEC = 30;

    /**
     * Reads the API key from the environment variable OPENAI_API_KEY.
     * Falls back to empty string (will cause a 401 from the API).
     */
    private final String apiKey;

    // Reusable HTTP client (thread-safe)
    private final HttpClient httpClient;

    // ─────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────

    /** Reads OPENAI_API_KEY from the environment. */
    public AiBusinessPlanAnalyzer() {
        this(System.getenv("OPENAI_API_KEY") != null
                ? System.getenv("OPENAI_API_KEY") : "");
    }

    /** Constructor for injecting a key directly (testing / configuration). */
    public AiBusinessPlanAnalyzer(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SEC))
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────

    /**
     * Sends the BusinessPlan to the AI API and returns structured feedback.
     *
     * @param plan the BusinessPlan to analyze (must not be null)
     * @return AI-generated feedback text
     * @throws AiAnalysisException if the API call fails, times out, or returns an error
     */
    public String analyzeBusinessPlan(BusinessPlan plan) throws AiAnalysisException {
        if (plan == null) throw new IllegalArgumentException("BusinessPlan must not be null.");

        if (apiKey == null || apiKey.isBlank()) {
            throw new AiAnalysisException(
                "API key not configured. Set the OPENAI_API_KEY environment variable.");
        }

        String prompt  = buildPrompt(plan);
        String payload = buildJsonPayload(prompt);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(TIMEOUT_SEC))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            return parseResponse(response);

        } catch (IOException ex) {
            throw new AiAnalysisException("Network error: " + ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AiAnalysisException("Request timed out or was interrupted.", ex);
        }
    }

    // ─────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────

    /**
     * Builds the natural-language prompt sent to the AI.
     * Includes all key BusinessPlan fields.
     */
    private String buildPrompt(BusinessPlan plan) {
        return "You are an expert startup investment analyst. " +
               "Analyze the following business plan and provide concise, structured feedback " +
               "covering: strengths, weaknesses, market opportunity, financial viability, " +
               "and an overall recommendation (Approve / Pending / Reject).\n\n" +
               "Business Plan Details:\n" +
               "Title:              " + nullSafe(plan.getTitle())             + "\n" +
               "Description:        " + nullSafe(plan.getDescription())       + "\n" +
               "Market Analysis:    " + nullSafe(plan.getMarketAnalysis())    + "\n" +
               "Financial Forecast: " + nullSafe(plan.getFinancialForecast()) + "\n" +
               "Funding Required:   $" + (plan.getFundingRequired() != null
                                          ? String.format("%,.0f", plan.getFundingRequired())
                                          : "N/A")                            + "\n" +
               "Timeline:           " + nullSafe(plan.getTimeline())          + "\n" +
               "Current Status:     " + nullSafe(plan.getStatus())            + "\n\n" +
               "Respond in clear paragraphs. Keep the total response under 350 words.";
    }

    /**
     * Wraps the prompt in the OpenAI Chat Completions JSON envelope.
     * Uses basic string building to avoid a JSON library dependency.
     */
    private String buildJsonPayload(String prompt) {
        // Escape double-quotes and newlines so the string fits inside JSON
        String escaped = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return "{"
             + "\"model\":\"" + MODEL + "\","
             + "\"messages\":["
             +   "{\"role\":\"user\",\"content\":\"" + escaped + "\"}"
             + "],"
             + "\"max_tokens\":500,"
             + "\"temperature\":0.7"
             + "}";
    }

    /**
     * Parses the OpenAI JSON response and extracts the first assistant message.
     *
     * Expected structure:
     * <pre>
     * {
     *   "choices": [{
     *     "message": { "content": "..." }
     *   }]
     * }
     * </pre>
     */
    private String parseResponse(HttpResponse<String> response) throws AiAnalysisException {
        int statusCode = response.statusCode();
        String body    = response.body();

        if (statusCode == 401) {
            throw new AiAnalysisException("Invalid API key. Check your OPENAI_API_KEY.");
        }
        if (statusCode == 429) {
            throw new AiAnalysisException("Rate limit exceeded. Please try again in a moment.");
        }
        if (statusCode != 200) {
            throw new AiAnalysisException(
                "API returned HTTP " + statusCode + ": " + truncate(body, 200));
        }
        if (body == null || body.isBlank()) {
            throw new AiAnalysisException("Empty response received from the AI API.");
        }

        // Simple JSON extraction without a library:
        // Find "content":"..." inside the first choice
        String contentKey = "\"content\":";
        int ci = body.indexOf(contentKey);
        if (ci == -1) {
            throw new AiAnalysisException("Unexpected response format: " + truncate(body, 200));
        }

        int start = body.indexOf('"', ci + contentKey.length()) + 1;
        int end   = findJsonStringEnd(body, start);
        if (start <= 0 || end <= start) {
            throw new AiAnalysisException("Could not extract content from response.");
        }

        String raw = body.substring(start, end);
        // Unescape common escape sequences
        return raw.replace("\\n", "\n")
                  .replace("\\r", "")
                  .replace("\\\"", "\"")
                  .replace("\\\\", "\\")
                  .trim();
    }

    /**
     * Walks forward from {@code from} in {@code s}, respecting \" escapes,
     * and returns the index of the closing unescaped quote.
     */
    private int findJsonStringEnd(String s, int from) {
        for (int i = from; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') { i++; continue; } // skip escaped char
            if (c == '"')  { return i; }
        }
        return -1;
    }

    private String nullSafe(String s) { return s != null ? s : "N/A"; }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    // ─────────────────────────────────────────────────────────
    // Checked exception
    // ─────────────────────────────────────────────────────────

    /**
     * Thrown when the AI analysis fails for any reason
     * (network, auth, parse error, etc.).
     */
    public static class AiAnalysisException extends Exception {
        public AiAnalysisException(String message) { super(message); }
        public AiAnalysisException(String message, Throwable cause) { super(message, cause); }
    }
}

