package com.aiparse.cli.service;

import com.aiparse.cli.exception.CliException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Thin client for the Qwen (DashScope) OpenAI-compatible chat completions
 * endpoint. Falls back to a deterministic mock response when --mock is set
 * or when the API key is not configured.
 */
public class AiService {
    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    public static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    public static final String DEFAULT_MODEL = "qwen-plus";

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final boolean mock;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public AiService(String apiKey, String baseUrl, String model, boolean mock) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl == null || baseUrl.isBlank() ? DEFAULT_BASE_URL : baseUrl;
        this.model = model == null || model.isBlank() ? DEFAULT_MODEL : model;
        this.mock = mock;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.mapper = new ObjectMapper();
    }

    public boolean isMock() {
        return mock || apiKey == null || apiKey.isBlank();
    }

    /**
     * Send a system+user prompt pair to the model and return the raw text of
     * the assistant message. Throws CliException on transport or API failure.
     */
    public String chat(String systemPrompt, String userPrompt) {
        if (isMock()) {
            throw new IllegalStateException("chat() called in mock mode");
        }

        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model", model);
            var messages = body.putArray("messages");
            var sys = messages.addObject();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            var usr = messages.addObject();
            usr.put("role", "user");
            usr.put("content", userPrompt);
            body.put("temperature", 0.2);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            log.debug("POST {} model={}", baseUrl, model);
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String respBody = response.body();
            if (status / 100 != 2) {
                log.error("AI call failed: status={} body={}", status, respBody);
                throw new CliException("AI call failed (HTTP " + status + "): " + truncate(respBody, 300), 4);
            }
            JsonNode root = mapper.readTree(respBody);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.isNull()) {
                throw new CliException("AI response missing message content", 4);
            }
            return content.asText();
        } catch (CliException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI call error", e);
            throw new CliException(4, "AI call error: " + e.getMessage(), e);
        }
    }

    private static String truncate(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, n) + "...";
    }
}
