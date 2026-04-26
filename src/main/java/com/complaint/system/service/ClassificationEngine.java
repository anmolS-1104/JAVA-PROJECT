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
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-8b:generateContent";

    public Department classify(String complaintText) {
        try {
            String prompt = "You are a complaint classification system. Read the following complaint and reply with ONLY one word — either 'Finance', 'Logistics', or 'Technical'. No explanation, just the word.\n\nComplaint: " + complaintText;
            String body = callGemini(prompt);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode candidates = mapper.readTree(body).path("candidates");
            if (!candidates.isMissingNode() && !candidates.isEmpty()) {
                String result = candidates.get(0)
                        .path("content").path("parts").get(0)
                        .path("text").asText("").trim();
                if (result.equalsIgnoreCase("Finance"))   return new FinanceDepartment();
                if (result.equalsIgnoreCase("Logistics")) return new LogisticsDepartment();
                if (result.equalsIgnoreCase("Technical")) return new TechnicalDepartment();
            }
        } catch (Exception e) {
            System.err.println("Gemini classify failed, using keyword fallback: " + e.getMessage());
        }
        return classifyByKeyword(complaintText);
    }

    public String getPriority(String complaintText) {
        try {
            String prompt = "You are a complaint priority classifier. Read the following complaint and reply with ONLY one word — either 'HIGH', 'MEDIUM', or 'NORMAL'. No explanation, just the word.\n\nComplaint: " + complaintText;
            String body = callGemini(prompt);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode candidates = mapper.readTree(body).path("candidates");
            if (!candidates.isMissingNode() && !candidates.isEmpty()) {
                String result = candidates.get(0)
                        .path("content").path("parts").get(0)
                        .path("text").asText("").trim().toUpperCase();
                if (result.equals("HIGH") || result.equals("MEDIUM") || result.equals("NORMAL")) return result;
            }
        } catch (Exception e) {
            System.err.println("Gemini priority failed, using keyword fallback: " + e.getMessage());
        }
        return priorityByKeyword(complaintText);
    }

    private Department classifyByKeyword(String text) {
        if (text == null || text.isBlank()) return new TechnicalDepartment();
        String lower = text.toLowerCase();
        if (lower.matches(".*(payment|refund|charge|transaction|invoice|billing|money|fee|bank|credit|debit).*"))
            return new FinanceDepartment();
        if (lower.matches(".*(deliver|package|shipment|courier|dispatch|tracking|arrived|late|order|parcel|logistics).*"))
            return new LogisticsDepartment();
        return new TechnicalDepartment();
    }

    private String priorityByKeyword(String text) {
        if (text == null || text.isBlank()) return "NORMAL";
        String lower = text.toLowerCase();
        if (lower.matches(".*(urgent|asap|immediately|demand|critical|emergency|broken|not working|failed).*"))
            return "HIGH";
        if (lower.matches(".*(issue|problem|slow|delay|error|trouble|not able|cannot).*"))
            return "MEDIUM";
        return "NORMAL";
    }

    private String callGemini(String prompt) throws Exception {
        String requestBody = """
    {
        "contents": [{
            "parts": [{"text": "%s"}]
        }],
        "generationConfig": {
            "maxOutputTokens": 50
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