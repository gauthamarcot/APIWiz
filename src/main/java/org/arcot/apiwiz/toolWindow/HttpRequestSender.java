package org.arcot.apiwiz.toolWindow;

import javax.swing.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

public class HttpRequestSender {
    public static void sendRequest(String url, String method, String headers, String body, JTextArea responseArea) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url));

            // Add headers
            if (!headers.isEmpty()) {
                Arrays.stream(headers.split("\n"))
                    .filter(header -> header.contains(":"))
                    .map(header -> header.split(":", 2))
                    .forEach(parts -> requestBuilder.header(parts[0].trim(), parts[1].trim()));
            }

            // Add method and body
            switch (method) {
                case "GET":
                    requestBuilder.GET();
                    break;
                case "POST":
                    requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
                    break;
                case "PUT":
                    requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(body));
                    break;
                case "DELETE":
                    requestBuilder.DELETE();
                    break;
            }

            // Send request asynchronously
            client.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    SwingUtilities.invokeLater(() -> {
                        StringBuilder result = new StringBuilder();
                        result.append("Status: ").append(response.statusCode()).append("\n\n");
                        result.append("Headers:\n");
                        response.headers().map().forEach((k, v) -> 
                            result.append(k).append(": ").append(v).append("\n")
                        );
                        result.append("\nBody:\n").append(response.body());
                        responseArea.setText(result.toString());
                    });
                })
                .exceptionally(e -> {
                    SwingUtilities.invokeLater(() -> 
                        responseArea.setText("Error: " + e.getMessage())
                    );
                    return null;
                });
        } catch (Exception e) {
            responseArea.setText("Error: " + e.getMessage());
        }
    }
} 