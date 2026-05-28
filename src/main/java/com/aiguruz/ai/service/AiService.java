package com.aiguruz.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final WebClient anthropicClient;

    @Value("${anthropic.model}")      private String model;
    @Value("${anthropic.max-tokens}") private int    maxTokens;

    public record Msg(String role, String content) {}

    public String complete(String system, List<Msg> messages) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("max_tokens", maxTokens);
        body.put("system", system);
        body.put("messages", messages.stream()
            .map(m -> Map.of("role", m.role(), "content", m.content()))
            .toList());

        log.debug("Calling Anthropic API: model={} messages={}", model, messages.size());

        Map<?, ?> resp = anthropicClient.post()
            .uri("/v1/messages")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        if (resp == null) throw new RuntimeException("No response from Anthropic");

        String reply = ((List<Map<?, ?>>) resp.get("content")).stream()
            .filter(c -> "text".equals(c.get("type")))
            .map(c -> (String) c.get("text"))
            .findFirst().orElse("");

        log.debug("Anthropic reply length: {} chars", reply.length());
        return reply;
    }

    public String completeJson(String system, String userMsg) {
        String enhanced = system +
            "\n\nCRITICAL INSTRUCTION: Your entire response must be valid JSON. " +
            "No markdown code fences, no preamble, no explanation — only the JSON object.";
        String raw = complete(enhanced, List.of(new Msg("user", userMsg)));
        return raw.replaceAll("(?s)^```json\\s*", "")
                  .replaceAll("(?s)\\s*```$", "")
                  .trim();
    }
}
