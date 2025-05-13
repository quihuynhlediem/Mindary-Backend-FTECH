package com.mindary.aichat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
public class GeminiApiKeyTest {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Test
    public void testGeminiApiKey() {
        String url = "https://generativelanguage.googleapis.com/v1beta/models";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        HttpEntity<String> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            String response = restTemplate.getForObject(url + "?key=" + apiKey, String.class);
            System.out.println("API Key is working! Available models: " + response);
        } catch (Exception e) {
            System.err.println("API Key test failed! Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
