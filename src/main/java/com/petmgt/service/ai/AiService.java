package com.petmgt.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.endpoint}")
    private String endpoint;

    @Value("${ai.deepseek.model:deepseek-chat}")
    private String model;

    private final RestTemplate restTemplate;

    public AiService(RestTemplate aiRestTemplate) {
        this.restTemplate = aiRestTemplate;
    }

    public String chat(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("AI API key 未配置，请设置环境变量 DEEPSEEK_API_KEY 或在 application.properties 中配置 ai.deepseek.api-key");
            return null;
        }

        try {
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userMessage)
                ),
                "temperature", 0.7
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, request, Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            String rootCause = e.getCause() != null ? e.getCause().toString() : e.toString();
            log.error("AI API 调用失败: {}", rootCause, e);
            return "AI 调用失败: " + rootCause;
        }
    }
}
