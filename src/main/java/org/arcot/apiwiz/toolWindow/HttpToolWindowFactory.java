package org.arcot.apiwiz.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

// Fix Swing imports
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create request panel
        JPanel requestPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // URL input
        JTextField urlField = new JTextField();
        JComboBox<String> methodCombo = new JComboBox<>(new String[]{"GET", "POST", "PUT", "DELETE"});
        
        // Headers panel
        JPanel headersPanel = new JPanel(new BorderLayout());
        headersPanel.setBorder(BorderFactory.createTitledBorder("Headers"));
        JTextArea headersArea = new JTextArea(4, 40);
        headersPanel.add(new JScrollPane(headersArea), BorderLayout.CENTER);
        
        // Request body
        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBorder(BorderFactory.createTitledBorder("Request Body"));
        JTextArea bodyArea = new JTextArea(8, 40);
        bodyPanel.add(new JScrollPane(bodyArea), BorderLayout.CENTER);
        
        // Response area
        JPanel responsePanel = new JPanel(new BorderLayout());
        responsePanel.setBorder(BorderFactory.createTitledBorder("Response"));
        JTextArea responseArea = new JTextArea(12, 40);
        responseArea.setEditable(false);
        responsePanel.add(new JScrollPane(responseArea), BorderLayout.CENTER);
        
        // Send button
        JButton sendButton = new JButton("Send Request");
        sendButton.addActionListener(e -> {
            try {
                // Create HTTP client
                HttpClient client = HttpClient.newHttpClient();
                
                // Build the request
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(urlField.getText()))
                    .method(methodCombo.getSelectedItem().toString(), 
                           bodyArea.getText().isEmpty() ? 
                           HttpRequest.BodyPublishers.noBody() : 
                           HttpRequest.BodyPublishers.ofString(bodyArea.getText()));

                // Add headers if present
                if (!headersArea.getText().isEmpty()) {
                    for (String headerLine : headersArea.getText().split("\n")) {
                        if (headerLine.contains(":")) {
                            String[] parts = headerLine.split(":", 2);
                            requestBuilder.header(parts[0].trim(), parts[1].trim());
                        }
                    }
                }

                // Send request
                HttpResponse<String> response = client.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

                // Show response
                StringBuilder responseText = new StringBuilder();
                responseText.append("Status Code: ").append(response.statusCode()).append("\n\n");
                responseText.append("Headers:\n");
                response.headers().map().forEach((k, v) -> 
                    responseText.append(k).append(": ").append(v).append("\n"));
                responseText.append("\nBody:\n").append(response.body());

                responseArea.setText(responseText.toString());

            } catch (Exception ex) {
                responseArea.setText("Error making request:\n" + ex.getMessage());
            }
        });
        
        // Add components to request panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        requestPanel.add(methodCombo, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        requestPanel.add(urlField, gbc);
        
        // Add all panels to main panel
        mainPanel.add(requestPanel, BorderLayout.NORTH);
        mainPanel.add(headersPanel, BorderLayout.CENTER);
        mainPanel.add(bodyPanel, BorderLayout.CENTER);
        mainPanel.add(sendButton, BorderLayout.CENTER);
        mainPanel.add(responsePanel, BorderLayout.SOUTH);
        
        // Add to tool window
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }
} 