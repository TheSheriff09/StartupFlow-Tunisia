package tn.esprit.Services;

import tn.esprit.entities.BusinessPlan;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * AIAnalyzerService — Sends a BusinessPlan to the Google Gemini API
 * and returns a fully structured {@link AIAnalysisResult}.
 *
 * API endpoint: POST https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent
 * Authentication: API key passed as a query parameter.
 *
 * The HTTP call is BLOCKING — always invoke from a background thread
 * (e.g. a JavaFX {@code Task<AIAnalysisResult>}).
 *
 * The prompt instructs the AI to respond in a deterministic section-based format
 * so the parser can reliably extract the four structured sections.
 */
public class AIAnalyzerService {

    // ── API constants ─────────────────────────────────────────
    private static final String  API_BASE_URL    = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final int     TIMEOUT_SECONDS = 30;
    private static final double  TEMPERATURE     = 0.4;
    private static final int     MAX_RETRIES     = 3;
    private static final long[]  RETRY_DELAYS_MS = {5_000L, 15_000L, 30_000L};

    // ── Section markers the prompt uses ──────────────────────
    // These must match the labels in buildPrompt() exactly.
    private static final String  SEC_STRENGTHS   = "STRENGTHS:";
    private static final String  SEC_WEAKNESSES  = "WEAKNESSES:";
    private static final String  SEC_RISK        = "RISK LEVEL:";
    private static final String  SEC_SUGGESTIONS = "SUGGESTIONS:";

    // ── State ─────────────────────────────────────────────────
    private final String     apiKey;
    private final HttpClient httpClient; // thread-safe, reusable

    // ─────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────

    // Hardcoded Gemini API key
    private static final String HARDCODED_KEY = "AIzaSyAyXNEfA-TtdsFqT52Lh7PLO82CK3EUdEg";

    /**
     * Default constructor: uses the hardcoded Gemini API key.
     */
    public AIAnalyzerService() {
        this(HARDCODED_KEY);
    }

    /**
     * Constructor for injecting an API key directly.
     *
     * @param apiKey your Gemini API key
     */
    public AIAnalyzerService(String apiKey) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────

    /**
     * Analyzes a BusinessPlan and returns a structured {@link AIAnalysisResult}.
     *
     * @param plan the BusinessPlan to analyze (must not be null)
     * @return a fully populated {@link AIAnalysisResult}
     * @throws AIAnalysisException if the call fails for any reason
     *         (no network, bad API key, timeout, unexpected response, etc.)
     */
    public AIAnalysisResult analyzeBusinessPlan(BusinessPlan plan) throws AIAnalysisException {
        // ── Guard: null input ────────────────────────────────
        if (plan == null) {
            throw new IllegalArgumentException("BusinessPlan must not be null.");
        }

        // ── Guard: API key missing ───────────────────────────
        if (apiKey.isEmpty()) {
            throw new AIAnalysisException(
                "Gemini API key is not configured.");
        }

        // ── Build HTTP request ───────────────────────────────
        String prompt  = buildPrompt(plan);
        String payload = buildJsonPayload(prompt);
        String url     = API_BASE_URL + "?key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        // ── Send and receive with retry on 429 ───────────────
        HttpResponse<String> response = null;
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException ex) {
                System.err.println("[AIAnalyzerService] Network error, using local analysis: " + ex.getMessage());
                return buildLocalAnalysis(plan);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return buildLocalAnalysis(plan);
            }

            if (response.statusCode() == 429) {
                if (attempt < MAX_RETRIES) {
                    long wait = RETRY_DELAYS_MS[attempt];
                    System.out.printf(
                        "[AIAnalyzerService] Rate-limited (429). Retrying in %d s (attempt %d/%d)…%n",
                        wait / 1000, attempt + 1, MAX_RETRIES);
                    try { Thread.sleep(wait); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                } else {
                    // All retries exhausted — fall back to local analysis
                    System.err.println("[AIAnalyzerService] API quota exceeded, using local analysis.");
                    return buildLocalAnalysis(plan);
                }
            }
            break;
        }

        // ── Validate HTTP status ─────────────────────────────
        validateHttpStatus(response);

        // ── Extract text from JSON, parse into result object ─
        String rawText = extractContent(response.body());
        return parseResult(rawText);
    }

    /**
     * Generates a meaningful analysis locally based on the plan's own data.
     * Used as a fallback when the Gemini API quota is exhausted.
     */
    private AIAnalysisResult buildLocalAnalysis(BusinessPlan plan) {
        List<String> strengths   = new ArrayList<>();
        List<String> weaknesses  = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        String riskLevel;

        // ── Strengths based on real plan data ────────────────
        if (plan.getFundingRequired() != null && plan.getFundingRequired() > 0) {
            strengths.add(String.format("Clear funding target defined: %,.0f TND", plan.getFundingRequired()));
        }
        if (plan.getMarketAnalysis() != null && plan.getMarketAnalysis().length() > 50) {
            strengths.add("Detailed market analysis demonstrates solid market understanding");
        }
        if (plan.getTimeline() != null && !plan.getTimeline().isBlank()) {
            strengths.add("Execution timeline is defined, showing planning maturity");
        }
        if (plan.getFinancialForecast() != null && !plan.getFinancialForecast().isBlank()) {
            strengths.add("Financial forecast present, aiding investor confidence");
        }
        if (plan.getDescription() != null && plan.getDescription().length() > 80) {
            strengths.add("Well-articulated business description covering core value proposition");
        }
        if (strengths.isEmpty()) {
            strengths.add("Business plan has foundational structure in place");
        }

        // ── Weaknesses based on missing/thin fields ──────────
        if (plan.getMarketAnalysis() == null || plan.getMarketAnalysis().length() < 50) {
            weaknesses.add("Market analysis is too brief — deeper competitive research is needed");
        }
        if (plan.getFinancialForecast() == null || plan.getFinancialForecast().isBlank()) {
            weaknesses.add("No financial forecast provided — investors require revenue projections");
        }
        if (plan.getTimeline() == null || plan.getTimeline().isBlank()) {
            weaknesses.add("Missing execution timeline — milestones and deadlines need to be defined");
        }
        if (plan.getFundingRequired() == null || plan.getFundingRequired() == 0) {
            weaknesses.add("Funding requirement not specified — budget planning is incomplete");
        }
        if (plan.getDescription() == null || plan.getDescription().length() < 40) {
            weaknesses.add("Business description is too vague — needs clearer value proposition");
        }
        if (weaknesses.isEmpty()) {
            weaknesses.add("Monitor execution closely to ensure planned milestones are met on time");
        }

        // ── Risk level based on completeness score ───────────
        long filledFields = List.of(
            plan.getTitle(), plan.getDescription(), plan.getMarketAnalysis(),
            plan.getFinancialForecast(), plan.getTimeline()
        ).stream().filter(s -> s != null && !s.isBlank()).count();
        boolean hasFunding = plan.getFundingRequired() != null && plan.getFundingRequired() > 0;
        int score = (int) filledFields + (hasFunding ? 1 : 0);

        if (score >= 5)      riskLevel = AIAnalysisResult.RISK_LOW;
        else if (score >= 3) riskLevel = AIAnalysisResult.RISK_MEDIUM;
        else                 riskLevel = AIAnalysisResult.RISK_HIGH;

        // ── Suggestions ───────────────────────────────────────
        suggestions.add("Conduct customer discovery interviews to validate the target market");
        suggestions.add("Define a clear go-to-market strategy with specific acquisition channels");
        suggestions.add("Detail monthly burn rate and break-even timeline in the financial section");
        if ("Draft".equalsIgnoreCase(plan.getStatus()) || plan.getStatus() == null) {
            suggestions.add("Move the plan to 'Active' status and begin pilot testing with early adopters");
        }
        suggestions.add("Add competitive landscape analysis comparing at least 3 direct competitors");

        String note = "(⚠ Generated locally — Gemini API quota exceeded. Replace API key for AI-powered analysis.)";

        return new AIAnalysisResult.Builder()
                .strengths(strengths)
                .weaknesses(weaknesses)
                .riskLevel(riskLevel)
                .suggestions(suggestions)
                .rawResponse(note)
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // Prompt engineering
    // ─────────────────────────────────────────────────────────

    /**
     * Builds a strict section-based prompt so the AI output is easy to parse.
     *
     * The prompt includes all key BusinessPlan fields and instructs the model
     * to respond ONLY with four labeled sections.
     */
    private String buildPrompt(BusinessPlan plan) {
        return "You are a senior startup investment analyst. " +
               "Analyze the business plan below and respond ONLY in the following exact format — " +
               "no extra text, no preamble:\n\n" +

               "STRENGTHS:\n- [strength 1]\n- [strength 2]\n\n" +
               "WEAKNESSES:\n- [weakness 1]\n- [weakness 2]\n\n" +
               "RISK LEVEL:\n[HIGH / MEDIUM / LOW]\n\n" +
               "SUGGESTIONS:\n- [suggestion 1]\n- [suggestion 2]\n\n" +

               "---\nBusiness Plan Details:\n" +
               "Title:              " + safe(plan.getTitle())             + "\n" +
               "Description:        " + safe(plan.getDescription())       + "\n" +
               "Market Analysis:    " + safe(plan.getMarketAnalysis())    + "\n" +
               "Financial Forecast: " + safe(plan.getFinancialForecast()) + "\n" +
               "Funding Required:   $" + (plan.getFundingRequired() != null
                                         ? String.format("%,.0f", plan.getFundingRequired()) : "N/A") + "\n" +
               "Timeline:           " + safe(plan.getTimeline())          + "\n" +
               "Current Status:     " + safe(plan.getStatus())            + "\n";
    }

    /**
     * Wraps the user prompt in the Gemini generateContent JSON envelope.
     */
    private String buildJsonPayload(String prompt) {
        String escaped = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\n")
                .replace("\t", "\\t");

        return "{"
             + "\"contents\":[{"
             +   "\"parts\":[{\"text\":\"" + escaped + "\"}]"
             + "}],"
             + "\"generationConfig\":{"
             +   "\"temperature\":" + TEMPERATURE
             + "}"
             + "}";
    }

    // ─────────────────────────────────────────────────────────
    // HTTP / JSON helpers
    // ─────────────────────────────────────────────────────────

    /** Throws a descriptive exception for non-200 HTTP statuses. */
    private void validateHttpStatus(HttpResponse<String> response) throws AIAnalysisException {
        int code = response.statusCode();
        switch (code) {
            case 200  -> { /* all good */ }
            case 400  -> throw new AIAnalysisException(
                             "Bad request (HTTP 400). The prompt may contain invalid content.");
            case 403  -> throw new AIAnalysisException(
                             "Invalid or unauthorized API key (HTTP 403). Check your Gemini key.");
            case 429  -> throw new AIAnalysisException(
                             "Rate limit exceeded after " + MAX_RETRIES + " retries.\n" +
                             "Your Gemini quota may be exhausted. Try again later.");
            case 500, 502, 503 -> throw new AIAnalysisException(
                             "Gemini server error (HTTP " + code + "). Please try again later.");
            default   -> throw new AIAnalysisException(
                             "Unexpected HTTP " + code + " response:\n"
                             + truncate(response.body(), 250));
        }
    }

    /**
     * Extracts the text field from the Gemini generateContent response.
     *
     * Expected JSON shape:
     * <pre>
     * { "candidates": [{ "content": { "parts": [{ "text": "..." }] } }] }
     * </pre>
     */
    private String extractContent(String body) throws AIAnalysisException {
        if (body == null || body.isBlank()) {
            throw new AIAnalysisException("Empty response body from the AI API.");
        }
        // Gemini response: candidates[0].content.parts[0].text
        final String key = "\"text\":";
        int ci = body.indexOf(key);
        if (ci == -1) {
            throw new AIAnalysisException(
                "Cannot find 'text' field in the Gemini response:\n" + truncate(body, 300));
        }
        int start = body.indexOf('"', ci + key.length()) + 1;
        int end   = findStringEnd(body, start);
        if (start <= 0 || end <= start) {
            throw new AIAnalysisException("Malformed 'text' value in Gemini response.");
        }
        return unescape(body.substring(start, end));
    }

    /**
     * Walks forward from {@code from} respecting \" escapes, returns the index
     * of the closing unescaped quote.
     */
    private int findStringEnd(String s, int from) {
        for (int i = from; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') { i++; continue; } // skip escaped char
            if (c == '"')  return i;
        }
        return -1;
    }

    /** Unescapes common JSON escape sequences in a string value. */
    private String unescape(String s) {
        return s.replace("\\n", "\n")
                .replace("\\r", "")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    // ─────────────────────────────────────────────────────────
    // Response parser
    // ─────────────────────────────────────────────────────────

    /**
     * Parses the free-text AI response into a structured {@link AIAnalysisResult}.
     *
     * Looks for the four section headers (STRENGTHS:, WEAKNESSES:,
     * RISK LEVEL:, SUGGESTIONS:) and collects their bullet lines.
     * Robust to minor formatting variations.
     */
    private AIAnalysisResult parseResult(String text) {
        List<String> strengths   = new ArrayList<>();
        List<String> weaknesses  = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        String       riskLevel   = "UNKNOWN";

        String currentSection = null;

        for (String rawLine : text.split("\\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            // ── Detect section header ────────────────────────
            String upper = line.toUpperCase();
            if (upper.startsWith(SEC_STRENGTHS)) {
                currentSection = SEC_STRENGTHS;
                continue;
            }
            if (upper.startsWith(SEC_WEAKNESSES)) {
                currentSection = SEC_WEAKNESSES;
                continue;
            }
            if (upper.startsWith(SEC_RISK)) {
                currentSection = SEC_RISK;
                // Risk level may appear on the same line after the colon
                String inline = line.substring(SEC_RISK.length()).trim();
                if (!inline.isEmpty()) {
                    riskLevel = normalizeRisk(inline);
                }
                continue;
            }
            if (upper.startsWith(SEC_SUGGESTIONS)) {
                currentSection = SEC_SUGGESTIONS;
                continue;
            }

            // ── Add line to current section ──────────────────
            if (currentSection == null) continue;

            switch (currentSection) {
                case SEC_STRENGTHS   -> strengths.add(strippedBullet(line));
                case SEC_WEAKNESSES  -> weaknesses.add(strippedBullet(line));
                case SEC_SUGGESTIONS -> suggestions.add(strippedBullet(line));
                case SEC_RISK        -> {
                    if (riskLevel.equals("UNKNOWN")) riskLevel = normalizeRisk(line);
                }
            }
        }

        return new AIAnalysisResult.Builder()
                .strengths(strengths)
                .weaknesses(weaknesses)
                .riskLevel(riskLevel)
                .suggestions(suggestions)
                .rawResponse(text)
                .build();
    }

    /** Strips leading bullet characters (-, •, *, numbers+dot) from a line. */
    private String strippedBullet(String line) {
        return line.replaceFirst("^[-•*]\\s*|^\\d+\\.\\s*", "").trim();
    }

    /**
     * Canonicalises freeform risk descriptions to HIGH / MEDIUM / LOW.
     * e.g. "Medium risk", "MEDIUM", "moderate" → "MEDIUM"
     */
    private String normalizeRisk(String raw) {
        String u = raw.toUpperCase();
        if (u.contains("HIGH")  || u.contains("CRITICAL")) return AIAnalysisResult.RISK_HIGH;
        if (u.contains("LOW")   || u.contains("MINIMAL"))  return AIAnalysisResult.RISK_LOW;
        if (u.contains("MEDIUM")|| u.contains("MODERATE")) return AIAnalysisResult.RISK_MEDIUM;
        return "UNKNOWN";
    }

    // ─────────────────────────────────────────────────────────
    // Utilities
    // ─────────────────────────────────────────────────────────

    private static String readApiKeyFromEnv() {
        String key = System.getenv("GEMINI_API_KEY");
        return key != null ? key : "";
    }

    private static String safe(String s) { return s != null ? s : "N/A"; }

    private static String truncate(String s, int max) {
        if (s == null)           return "(null)";
        if (s.length() <= max)   return s;
        return s.substring(0, max) + "…";
    }

    // ─────────────────────────────────────────────────────────
    // Checked exception
    // ─────────────────────────────────────────────────────────

    /**
     * Thrown by {@link #analyzeBusinessPlan} when the analysis cannot complete.
     * Covers network errors, authentication failures, API errors, and parse errors.
     */
    public static final class AIAnalysisException extends Exception {
        public AIAnalysisException(String message)                    { super(message); }
        public AIAnalysisException(String message, Throwable cause)   { super(message, cause); }
    }
}

