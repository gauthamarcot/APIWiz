package org.arcot.apiwiz.toolWindow;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlaskProcessListener extends ProcessAdapter {
    private final JTextArea responseArea;
    private final JTextField urlField;
    private static final Pattern FLASK_URL_PATTERN = 
        Pattern.compile("Running on (http://[^\\s]+)");

    public FlaskProcessListener(JTextArea responseArea, JTextField urlField) {
        this.responseArea = responseArea;
        this.urlField = urlField;
    }

    @Override
    public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
        String text = event.getText();
        
        // Look for Flask startup message
        if (text.contains("Running on")) {
            Matcher matcher = FLASK_URL_PATTERN.matcher(text);
            if (matcher.find()) {
                String baseUrl = matcher.group(1);
                updateUIWithFlaskRoutes(baseUrl);
            }
        }
    }

    private void updateUIWithFlaskRoutes(String baseUrl) {
        // Update UI on EDT
        SwingUtilities.invokeLater(() -> {
            urlField.setText(baseUrl);
            
            // Make a request to Flask routes
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/"))
                    .GET()
                    .build();

                HttpResponse<String> response = client.send(request, 
                    HttpResponse.BodyHandlers.ofString());

                StringBuilder routeInfo = new StringBuilder();
                routeInfo.append("Flask Application Detected!\n");
                routeInfo.append("Base URL: ").append(baseUrl).append("\n\n");
                routeInfo.append("Available Routes:\n");
                // Add basic route info
                routeInfo.append("/ (GET)\n");
                
                responseArea.setText(routeInfo.toString());

            } catch (Exception e) {
                responseArea.setText("Error connecting to Flask application: " + 
                    e.getMessage());
            }
        });
    }
} 