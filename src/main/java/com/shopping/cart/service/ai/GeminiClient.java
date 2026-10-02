package com.shopping.cart.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Thin wrapper over the Gemini REST API. The API key stays on the server so the web shop,
 * iOS app and admin never ship it.
 */
@Service
public class GeminiClient {
    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
    /** Overload errors come back in ~1-2s, so a second pass over the models is cheap. */
    private static final int MAX_PASSES = 2;
    private static final long RETRY_DELAY_MS = 1_500;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;
    private final String apiKey;
    private final List<String> models;

    public GeminiClient(
            @Value("${app.ai.gemini.api-key:}") String apiKey,
            @Value("${app.ai.gemini.model:gemini-3.8-flash}") String model,
            @Value("${app.ai.gemini.fallback-models:gemini-3.6-flash,gemini-flash-latest}") String fallbackModels) {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.models = new ArrayList<>(List.of(model.trim()));
        for (String fallback : (fallbackModels == null ? "" : fallbackModels).split(",")) {
            String name = fallback.trim();
            if (!name.isEmpty() && !models.contains(name)) {
                models.add(name);
            }
        }
    }

    public boolean isConfigured() {
        return !apiKey.isEmpty();
    }

    /**
     * Sends one prompt and returns the model's JSON reply, constrained to {@code responseSchema}
     * (Gemini's OpenAPI-style schema: types are "OBJECT", "ARRAY", "STRING", ...).
     * Each model has its own quota and load, so when one is overloaded, rate-limited or retired
     * the next fallback is tried.
     */
    public JsonNode generateJson(String systemInstruction, String prompt, JsonNode responseSchema) {
        if (!isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI features are not configured.");
        }

        ObjectNode body = objectMapper.createObjectNode();
        body.putObject("systemInstruction").putArray("parts").addObject().put("text", systemInstruction);
        ObjectNode content = body.putArray("contents").addObject();
        content.put("role", "user");
        content.putArray("parts").addObject().put("text", prompt);
        ObjectNode generationConfig = body.putObject("generationConfig");
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.set("responseSchema", responseSchema);

        try {
            String payload = objectMapper.writeValueAsString(body);
            List<String> candidates = new ArrayList<>(models);
            for (int pass = 0; pass < MAX_PASSES && !candidates.isEmpty(); pass++) {
                if (pass > 0) {
                    Thread.sleep(RETRY_DELAY_MS);
                }
                for (String model : List.copyOf(candidates)) {
                    HttpResponse<String> response = send(model, payload);
                    int status = response.statusCode();
                    if (status == 200) {
                        return parseReply(response.body());
                    }
                    log.warn("Gemini {} returned HTTP {}: {}", model, status, truncate(response.body()));
                    if (status == 404) {
                        candidates.remove(model);
                    } else if (!isOverloaded(status)) {
                        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI service failed. Try again shortly.");
                    }
                }
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI is busy right now. Try again shortly.");
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI request was interrupted.");
        } catch (Exception ex) {
            log.warn("Gemini request failed: {}", ex.toString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI service failed. Try again shortly.");
        }
    }

    private HttpResponse<String> send(String model, String payload) throws Exception {
        String url = ENDPOINT.formatted(URLEncoder.encode(model, StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(25))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode parseReply(String responseBody) throws Exception {
        JsonNode text = objectMapper.readTree(responseBody)
                .path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (!text.isTextual()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned an empty answer.");
        }
        return objectMapper.readTree(text.asText());
    }

    private static boolean isOverloaded(int status) {
        return status == 429 || status == 500 || status == 503 || status == 504;
    }

    private static String truncate(String s) {
        return s == null ? "" : s.length() > 300 ? s.substring(0, 300) : s;
    }
}
