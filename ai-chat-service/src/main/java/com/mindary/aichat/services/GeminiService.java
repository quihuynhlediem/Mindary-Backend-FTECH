package com.mindary.aichat.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindary.aichat.config.GeminiConfig;
import com.mindary.aichat.models.FollowUpAnalysis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;
    private final EmbeddingService embeddingService;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
    private static final String THERAPIST_PROMPT = """
            You are a compassionate, professional mental health therapist. Your responses should be:
            - Warm, empathetic, and supportive
            - Professional, but approachable
            - Provide clear, evidence-based advice
            - Use reflective listening
            - Keep responses concise unless the user asks for more detail
            If the user requests a detailed explanation, provide a longer, structured response. Otherwise, keep it short and focused.
            """;

    private static final String HOMIE_PROMPT = """
            You are the user's supportive best friend ("homie"). Your responses should be:
            - Super casual, friendly, and relatable
            - Use slang, emojis, and humor (if appropriate)
            - Give short, practical advice
            - Never sound like a robot or therapist
            If the user asks for more details, give a longer but still casual explanation. Otherwise, keep it brief and chill.
            """;

    public String generateResponse(String message, String conversationId, String diaryInsight, String mode) {
        String selectedMode = (mode == null || (!mode.equalsIgnoreCase("homie") && !mode.equalsIgnoreCase("therapist"))) ? "therapist" : mode.toLowerCase();
        try {
            StringBuilder promptBuilder = new StringBuilder(selectedMode.equals("homie") ? HOMIE_PROMPT : THERAPIST_PROMPT);

            // Get context through EmbeddingService directly
            if (conversationId != null && !conversationId.isEmpty()) {
                List<String> similarMessages = embeddingService.findSimilarMessages(
                        conversationId,
                        message,
                        3 // 3 for more focused context
                );
                if (!similarMessages.isEmpty()) {
                    promptBuilder.append("\nPrevious conversation context:\n")
                            .append(String.join("\n\n", similarMessages))
                            .append("\n");
                }
            }

            if (diaryInsight != null && !diaryInsight.isEmpty()) {
                promptBuilder.append("\nBased on your diary entries, I've noticed:\n")
                        .append(diaryInsight)
                        .append("\n\nLet me respond to your message while keeping these insights in mind.");
            }

            promptBuilder.append("\nUser's message: ").append(message);

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiConfig.getApiKey());

            // Prepare request body with adjusted parameters for more natural responses
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            contents.put("parts", new Object[]{
                Map.of("text", promptBuilder.toString())
            });
            requestBody.put("contents", new Object[]{contents});
            requestBody.put("generationConfig", Map.of(
                    "temperature", selectedMode.equals("homie") ? 0.95 : 0.85, // Homie is more random
                    "topK", selectedMode.equals("homie") ? 50 : 45,
                    "topP", 0.95,
                    "maxOutputTokens", 600 // Longer for detailed, shorter for normal
            ));

            // Make request
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(
                    GEMINI_API_URL + "?key=" + geminiConfig.getApiKey(),
                    request,
                    String.class
            );

            // Parse response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

        } catch (Exception e) {
            log.error("Error generating response from Gemini", e);
            return selectedMode.equals("homie")
                    ? "Yo, I'm having some trouble connecting right now, but I'm here for you! Wanna try sending that again?"
                    : "I'm having trouble connecting right now, but I'm here for you. Would you like to try sharing that again? I'm really interested in what you have to say.";
        }
    }

    public String generateConversationTitle(String message) {
        try {
            String prompt = "Generate a short, concise title (max 5 words) for a conversation that starts with this message: " + message;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiConfig.getApiKey());

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            contents.put("parts", new Object[]{
                Map.of("text", prompt)
            });
            requestBody.put("contents", new Object[]{contents});

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(
                    GEMINI_API_URL + "?key=" + geminiConfig.getApiKey(),
                    request,
                    String.class
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        } catch (Exception e) {
            log.error("Error generating conversation title", e);
            return "New Conversation";
        }
    }

    public FollowUpAnalysis analyzeForFollowUp(String message) {
        try {
            String prompt = """
                Analyze this message for potential follow-up needs. Consider:
                1. Health issues that need checking
                2. Anxiety or stress indicators
                3. Sleep-related problems
                4. Mood concerns
                
                Return a JSON with:
                {
                    "needsFollowUp": true/false,
                    "followUpType": "HEALTH_CHECK"/"ANXIETY_CHECK"/"SLEEP_CHECK"/"MOOD_CHECK"/"GENERAL_CHECK",
                    "followUpHours": number (between 8-24)
                }
                
                Message: """ + message;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiConfig.getApiKey());

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            contents.put("parts", new Object[]{
                Map.of("text", prompt)
            });
            requestBody.put("contents", new Object[]{contents});

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(
                    GEMINI_API_URL + "?key=" + geminiConfig.getApiKey(),
                    request,
                    String.class
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            String analysisJson = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            return mapper.readValue(analysisJson, FollowUpAnalysis.class);
        } catch (Exception e) {
            log.error("Error analyzing for follow-up", e);
            FollowUpAnalysis analysis = new FollowUpAnalysis();
            analysis.setNeedsFollowUp(false);
            return analysis;
        }
    }
}
