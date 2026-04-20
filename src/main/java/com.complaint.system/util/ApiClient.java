package com.complaint.system.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {
    // 🔹 This matches the @RequestMapping("/api/auth") in your Backend UserController
    private static final String API_BASE = "http://localhost:8080/api/auth";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static HttpResponse<String> post(String endpoint, String jsonPayload) throws Exception {
        // Ensures exactly one slash between BASE and endpoint
        String cleanEndpoint = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        String fullUrl = API_BASE + cleanEndpoint;

        System.out.println("🔗 Connecting to: " + fullUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> put(String s, String payload) {
        return null;
    }

    public static HttpResponse<String> get(String endpoint) {
        return null;
    }
}