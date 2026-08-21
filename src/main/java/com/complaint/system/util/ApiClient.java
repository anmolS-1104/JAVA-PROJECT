package com.complaint.system.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    private static final String BASE_URL = "http://complaint-system-backend-env.eba-3fbgyqbk.ap-south-1.elasticbeanstalk.com";

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Static Methods for FXML Controllers
    public static HttpResponse<String> post(String endpoint, String jsonPayload) throws IOException, InterruptedException {
        String fullUrl = BASE_URL + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload != null ? jsonPayload : ""))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> get(String endpoint) throws IOException, InterruptedException {
        String fullUrl = BASE_URL + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> put(String endpoint, String jsonPayload) throws IOException, InterruptedException {
        String fullUrl = BASE_URL + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload != null ? jsonPayload : ""))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> delete(String endpoint) throws IOException, InterruptedException {
        String fullUrl = BASE_URL + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .timeout(Duration.ofSeconds(15))
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // Async Instance Methods for ComplaintApp
    public CompletableFuture<String> getAllComplaints() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/complaints"))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    public CompletableFuture<String> submitComplaint(int userId, String desc, String priority, String dept, String notes) {
        String escapedDesc = desc.replace("\"", "\\\"").replace("\n", "\\n");
        String escapedNotes = (notes != null) ? notes.replace("\"", "\\\"").replace("\n", "\\n") : "";

        String json = String.format(
                "{\"userId\":%d,\"description\":\"%s\",\"priority\":\"%s\",\"department\":\"%s\",\"notes\":\"%s\"}",
                userId, escapedDesc, priority, dept, escapedNotes
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/complaints"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
}