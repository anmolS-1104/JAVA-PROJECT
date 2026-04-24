package com.complaint.system.service;

import com.complaint.system.model.Department;
import com.complaint.system.model.FinanceDepartment;
import com.complaint.system.model.LogisticsDepartment;
import com.complaint.system.model.TechnicalDepartment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClassificationEngine {

    private static final String API_KEY = "your_api_key";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-04-17:generateContent";

    public Department classify(String complaintText) {
        try {
            String prompt = "You are a complaint classification system. Read the following complaint and reply with ONLY one word — either 'Finance', 'Logistics', or 'Technical'. No explanation, just the word.\n\nComplaint: " + complaintText;
            String body = callGemini(prompt);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode candidates = mapper.readTree(body).path("candidates");
            if (candidates.isMissingNode() || candidates.isEmpty()) {
                System.err.println("Gemini classify no candidates: " + body);
                return new TechnicalDepartment();
            }
            String result = candidates.get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText("Technical").trim();

            if (result.equalsIgnoreCase("Finance")) return new FinanceDepartment();
            if (result.equalsIgnoreCase("Logistics")) return new LogisticsDepartment();
            return new TechnicalDepartment();

        } catch (Exception e) {
            System.err.println("AI classification failed, defaulting to Technical: " + e.getMessage());
            return new TechnicalDepartment();
        }
    }

    public String getPriority(String complaintText) {
        try {
            String prompt = "You are a complaint priority classifier. Read the following complaint and reply with ONLY one word — either 'HIGH', 'MEDIUM', or 'NORMAL'. No explanation, just the word.\n\nComplaint: " + complaintText;
            String body = callGemini(prompt);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode candidates = mapper.readTree(body).path("candidates");
            if (candidates.isMissingNode() || candidates.isEmpty()) {
                System.err.println("Gemini priority no candidates: " + body);
                return "NORMAL";
            }
            String result = candidates.get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText("NORMAL").trim();

            if (result.equalsIgnoreCase("HIGH")) return "HIGH";
            if (result.equalsIgnoreCase("MEDIUM")) return "MEDIUM";
            return "NORMAL";

        } catch (Exception e) {
            System.err.println("AI priority failed, defaulting to NORMAL: " + e.getMessage());
            return "NORMAL";
        }
    }

    private String callGemini(String prompt) throws Exception {
        String requestBody = """
        {
            "contents": [{
                "parts": [{"text": "%s"}]
            }],
            "generationConfig": {
                "maxOutputTokens": 50
            },
            "thinkingConfig": {
                "thinkingBudget": 0
            }
        }
        """.formatted(prompt.replace("\"", "\\\"").replace("\n", "\\n"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        System.err.println("Gemini raw: " + body);
        return body;
    }
}