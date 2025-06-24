package com.IndiaNIC.mood_predictor.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiService {
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final ObjectMapper objectMapper;

    public GeminiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }
    public String generateCheckinQuestions() {
        String url = GEMINI_API_BASE_URL + "?key=" + geminiApiKey;

        // Construct the request body as a Java Map, then convert to JSON string
        // This defines the structure and prompt for the Gemini API
        String requestBody = "{\n" +
                "    \"contents\": [\n" +
                "        {\n" +
                "            \"role\": \"user\",\n" +
                "            \"parts\": [\n" +
                "                {\"text\": \"Generate 10 concise, reflective yes/no or yes/no/maybe questions for a daily mood check-in. Provide them as a JSON array where each object has a 'question' field and an 'answer_type' field (either 'yes_no' or 'yes_no_maybe'). Example: [{'question': 'Did you feel energetic today?', 'answer_type': 'yes_no'}, ...]. Ensure only the JSON array is returned, no pre-text or post-text.\"\n}" +
                "            ]\n" +
                "        }\n" +
                "    ],\n" +
                "    \"generationConfig\": {\n" +
                "        \"responseMimeType\": \"application/json\",\n" +
                "        \"responseSchema\": {\n" +
                "            \"type\": \"ARRAY\",\n" +
                "            \"items\": {\n" +
                "                \"type\": \"OBJECT\",\n" +
                "                \"properties\": {\n" +
                "                    \"question\": { \"type\": \"STRING\" },\n" +
                "                    \"answer_type\": { \"type\": \"STRING\", \"enum\": [\"yes_no\", \"yes_no_maybe\"] }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // Set Content-Type header

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers); // Create HTTP entity with body and headers

        try {
            // Make the POST request and get the raw JSON response
            String response = restTemplate.postForObject(url, entity, String.class);
            if (response == null || response.isEmpty()) {
                throw new RuntimeException("Gemini API returned an empty or null response for questions.");
            }

            // Parse the response to extract the actual JSON array of questions
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode content = candidates.get(0).path("content");
                if (content.has("parts") && content.path("parts").isArray() && !content.path("parts").isEmpty()) {
                    JsonNode textNode = content.path("parts").get(0).path("text");
                    if (textNode.isTextual()) {
                        return textNode.asText();
                    }
                }
            }
            throw new RuntimeException("Failed to parse questions from Gemini API response: " + response);

        } catch (Exception e) {
            System.err.println("Error calling Gemini API for questions: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get questions from AI. " + e.getMessage(), e);
        }
    }

    public String predictMood(String questionsJson, String answersJson) {
        String url = GEMINI_API_BASE_URL + "?key=" + geminiApiKey;

        // Construct the request body as a Java Map, then convert to JSON string
        String prompt = "Given the following questions and user answers, classify the user's overall mood as either 'Flat', 'Happy', or 'Sad'. Only return the mood string (e.g., 'Happy'), no other text. \n\nQuestions: " + questionsJson + "\nAnswers: " + answersJson;

        String requestBody = "{\n" +
                "    \"contents\": [\n" +
                "        {\n" +
                "            \"role\": \"user\",\n" +
                "            \"parts\": [\n" +
                "                {\"text\": \"" + prompt.replace("\"", "\\\"") + "\"}\n" + // Escape double quotes in prompt
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            String response = restTemplate.postForObject(url, entity, String.class);
            if (response == null || response.isEmpty()) {
                throw new RuntimeException("Gemini API returned an empty or null response for mood prediction.");
            }

            // Parse the response to extract the predicted mood
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode content = candidates.get(0).path("content");
                if (content.has("parts") && content.path("parts").isArray() && !content.path("parts").isEmpty()) {
                    JsonNode textNode = content.path("parts").get(0).path("text");
                    if (textNode.isTextual()) {
                        // The 'text' field should contain only the mood string
                        return textNode.asText().trim(); // Trim to remove any leading/trailing whitespace
                    }
                }
            }
            throw new RuntimeException("Failed to parse predicted mood from Gemini API response: " + response);

        } catch (Exception e) {
            System.err.println("Error calling Gemini API for mood prediction: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to predict mood from AI. " + e.getMessage(), e);
        }
    }
}
