package com.IndiaNIC.mood_predictor.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String generateCheckinQuestions() {
        // --- FIX START: Simplified prompt for question generation ---
        String prompt = "Generate 10 daily mood check-in questions. Each question should be concise and end with a question mark. Each must have an 'answer_type' which is 'yes_no' or 'yes_no_maybe'. Return only the JSON array and give only the question.";
        // --- FIX END ---

        // Call callGeminiApi expecting a JSON result (true)
        return callGeminiApi(prompt, true);
    }

    public String predictMood(String questionsJson, String answersJson) {
        String prompt = "Given the following daily check-in questions and answers:\n" +
                "Questions: " + questionsJson + "\n" +
                "Answers: " + answersJson + "\n" +
                "Based on these, predict the user's overall mood today. Respond with only one word: 'Happy', 'Sad', or 'Flat'.";

        // Call callGeminiApi expecting a plain text result (false)
        return callGeminiApi(prompt, false);
    }

    // Added expectJsonResult parameter to conditionally handle JSON parsing
    private String callGeminiApi(String prompt, boolean expectJsonResult) {
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("role", "user");
        content.put("parts", Collections.singletonList(part));

        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", Collections.singletonList(content));

        // Only add generationConfig if expecting JSON, and it's the question generation prompt
        // (using the simplified prompt text for the check here)
        if (expectJsonResult && prompt.contains("Generate 10 daily mood check-in questions.")) {
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("responseMimeType", "application/json");
            Map<String, Object> responseSchema = new HashMap<>();
            responseSchema.put("type", "ARRAY");
            Map<String, Object> items = new HashMap<>();
            items.put("type", "OBJECT");
            Map<String, Object> properties = new HashMap<>();
            properties.put("question", Collections.singletonMap("type", "STRING"));
            properties.put("answer_type", Map.of("type", "STRING", "enum", List.of("yes_no", "yes_no_maybe")));
            items.put("properties", properties);
            generationConfig.put("responseSchema", responseSchema);
            responseSchema.put("items", items);
            payload.put("generationConfig", generationConfig);
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            Map<String, Object> apiResponse = restTemplate.postForObject(apiUrl, request, Map.class);

            if (apiResponse != null && apiResponse.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) apiResponse.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    if (firstCandidate.containsKey("content")) {
                        Map<String, Object> contentMap = (Map<String, Object>) firstCandidate.get("content");
                        if (contentMap.containsKey("parts")) {
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                            if (!parts.isEmpty()) {
                                String rawText = (String) parts.get(0).get("text");

                                // Trim whitespace just in case
                                rawText = rawText.trim();

                                // --- Conditional JSON Handling START ---
                                if (expectJsonResult) {
                                    // Strip Markdown fences only if we expect JSON
                                    rawText = rawText.replaceAll("```json\\n?", "").replaceAll("\\n```$", "");
                                    rawText = rawText.replaceAll("```typescript\\n?", "").replaceAll("\\n```$", "");
                                    rawText = rawText.replaceAll("```text\\n?", "").replaceAll("\\n```$", "");
                                    rawText = rawText.replaceAll("```\\n?", "").replaceAll("\\n```$", "");
                                    rawText = rawText.trim(); // Re-trim after stripping

                                    try {
                                        JsonNode jsonNode = objectMapper.readTree(rawText);
                                        return objectMapper.writeValueAsString(jsonNode);
                                    } catch (Exception e) {
                                        System.err.println("Error parsing/serializing LLM response to ensure proper JSON: " + e.getMessage());
                                        System.err.println("Raw text that caused parsing error: " + rawText);
                                        // Fallback to raw text if parsing fails, but log the error
                                        return rawText;
                                    }
                                } else {
                                    // If not expecting JSON (e.g., mood prediction), return raw text directly
                                    return rawText;
                                }
                                // --- Conditional JSON Handling END ---
                            }
                        }
                    }
                }
            }
            throw new RuntimeException("Gemini API response did not contain expected content or was empty.");
        } catch (Exception e) {
            System.err.println("Error calling Gemini API: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get response from Gemini API: " + e.getMessage(), e);
        }
    }
}
