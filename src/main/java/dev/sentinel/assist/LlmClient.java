package dev.sentinel.assist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@Slf4j
public class LlmClient {

    private final RestClient restClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${sentinel.llm.api-key:}")
    private String apiKey;

    @Value("${sentinel.llm.model}")
    private String model;

    @Value("${sentinel.llm.enabled:false}")
    private boolean enabled;

    public LlmClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.anthropic.com/v1")
                .build();
    }

    public boolean isAvailable() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    /**
     * Sends a single-turn prompt and returns the model's text response.
     * Returns empty on any failure — callers must treat this as best-effort,
     * never as a required step (see AssistService for the calling convention).
     */
    public java.util.Optional<String> complete(String prompt) {
        if (!isAvailable()) {
            return java.util.Optional.empty();
        }

        try {
            String responseBody = restClient.post()
                    .uri("/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("Content-Type", "application/json")
                    .body(Map.of(
                            "model", model,
                            "max_tokens", 300,
                            "messages", java.util.List.of(
                                    Map.of("role", "user", "content", prompt))
                    ))
                    .retrieve()
                    .body(String.class);

            JsonNode root = mapper.readTree(responseBody);
            String text = root.path("content").path(0).path("text").asText(null);
            return java.util.Optional.ofNullable(text);

        } catch (Exception e) {
            log.warn("LLM call failed, degrading to no-suggestion: {}", e.getMessage());
            return java.util.Optional.empty();
        }
    }
}