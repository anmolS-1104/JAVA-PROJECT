package com.complaint.system.service;

import com.complaint.system.model.Department;
import com.complaint.system.model.FinanceDepartment;
import com.complaint.system.model.LogisticsDepartment;
import com.complaint.system.model.TechnicalDepartment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ClassificationEngine {

    private static final String API_KEY = "your-anthropic-api-key-here";
    private static final String API_URL = "https://api.anthropic.com/v1/messages";

    public Department classify(String complaintText) {
        try {
            String prompt = "You are a complaint classification system. Read the following complaint and reply with ONLY one word — either 'Finance', 'Logistics', or 'Technical'. No explanation, just the word.\n\nComplaint: " + complaintText;

            String requestBody = """
                {
                    "model": "claude-haiku-4-5-20251001",
                    "max_tokens": 10,
                    "messages": [{"role": "user", "content": "%s"}]
                }
                """.formatted(prompt.replace("\"", "\\\"").replace("\n", "\\n"));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", API_KEY)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            // Extract the text value from the response
            String result = body.split("\"text\":\"")[1].split("\"")[0].trim();

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

            String requestBody = """
                {
                    "model": "claude-haiku-4-5-20251001",
                    "max_tokens": 10,
                    "messages": [{"role": "user", "content": "%s"}]
                }
                """.formatted(prompt.replace("\"", "\\\"").replace("\n", "\\n"));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", API_KEY)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            String result = body.split("\"text\":\"")[1].split("\"")[0].trim();

            if (result.equalsIgnoreCase("HIGH")) return "HIGH";
            if (result.equalsIgnoreCase("MEDIUM")) return "MEDIUM";
            return "NORMAL";

        } catch (Exception e) {
            System.err.println("AI priority failed, defaulting to NORMAL: " + e.getMessage());
            return "NORMAL";
        }
    }
}