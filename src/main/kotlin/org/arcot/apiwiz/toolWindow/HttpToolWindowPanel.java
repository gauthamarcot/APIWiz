package org.arcot.apiwiz.toolWindow;

import javax.swing.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBButton;
import com.intellij.util.ui.JBUI;
import org.arcot.apiwiz.services.HttpService;
import org.arcot.apiwiz.services.SwaggerService;
import org.arcot.apiwiz.services.SwaggerService.Endpoint;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.List;

public class HttpToolWindowPanel extends JPanel {
    private final Project project;
    private final JTextArea requestHeaders;
    private final JTextArea requestBody;
    private final JTextArea responseHeaders;
    private final JTextArea responseBody;
    private final JList<String> endpointsList;
    private final DefaultListModel<String> endpointsModel;
    private final SwaggerService swaggerService;

    public HttpToolWindowPanel(Project project) {
        super(new BorderLayout());
        this.project = project;
        this.swaggerService = new SwaggerService();
        this.endpointsModel = new DefaultListModel<>();
        this.endpointsList = new JList<>(endpointsModel);

        // Create main panels
        JPanel leftPanel = createLeftPanel();
        JPanel rightPanel = createRightPanel();
        JPanel bottomPanel = createBottomPanel();

        // Add panels to main layout
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Endpoints"));
        panel.setPreferredSize(new Dimension(300, 0));

        // Add Swagger import section
        JPanel importPanel = new JPanel(new VerticalFlowLayout());
        JBTextField swaggerUrlField = new JBTextField();
        JBButton importSwaggerButton = new JBButton("Import from Swagger");
        JBTextField flaskApiUrlField = new JBTextField();
        JBButton scanFlaskButton = new JBButton("Scan Flask API");

        importPanel.add(new JBLabel("Swagger URL:"));
        importPanel.add(swaggerUrlField);
        importPanel.add(importSwaggerButton);
        importPanel.add(new JBLabel("Flask API URL:"));
        importPanel.add(flaskApiUrlField);
        importPanel.add(scanFlaskButton);

        // Add endpoints list
        JBScrollPane scrollPane = new JBScrollPane(endpointsList);

        // Add action listeners
        importSwaggerButton.addActionListener(e -> {
            try {
                String swaggerUrl = swaggerUrlField.getText();
                if (swaggerUrl.isEmpty()) {
                    showError("Please enter a Swagger URL");
                    return;
                }
                swaggerService.importFromSwagger(swaggerUrl);
                updateEndpointsList();
            } catch (Exception ex) {
                showError("Failed to import Swagger: " + ex.getMessage());
            }
        });

        scanFlaskButton.addActionListener(e -> {
            try {
                String flaskApiUrl = flaskApiUrlField.getText();
                if (flaskApiUrl.isEmpty()) {
                    showError("Please enter a Flask API URL");
                    return;
                }
                swaggerService.scanFlaskApi(flaskApiUrl);
                updateEndpointsList();
            } catch (Exception ex) {
                showError("Failed to scan Flask API: " + ex.getMessage());
            }
        });

        endpointsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && endpointsList.getSelectedValue() != null) {
                String selected = endpointsList.getSelectedValue();
                String[] parts = selected.split(" ", 2);
                if (parts.length == 2) {
                    setMethod(parts[0]);
                    setUrl(parts[1]);
                }
            }
        });

        panel.add(importPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Request"));

        JPanel requestPanel = new JPanel(new VerticalFlowLayout());
        
        JBTextField urlField = new JBTextField();
        ComboBox<String> methodComboBox = new ComboBox<>(new String[]{"GET", "POST", "PUT", "DELETE", "PATCH"});
        requestHeaders = new JBTextArea(5, 50);
        requestBody = new JBTextArea(10, 50);
        JBButton sendButton = new JBButton("Send Request");

        requestPanel.add(new JBLabel("URL:"));
        requestPanel.add(urlField);
        requestPanel.add(new JBLabel("Method:"));
        requestPanel.add(methodComboBox);
        requestPanel.add(new JBLabel("Request Headers:"));
        requestPanel.add(new JBScrollPane(requestHeaders));
        requestPanel.add(new JBLabel("Request Body:"));
        requestPanel.add(new JBScrollPane(requestBody));
        requestPanel.add(sendButton);

        sendButton.addActionListener(e -> {
            try {
                String url = urlField.getText();
                if (url.isEmpty()) {
                    showError("Please enter a URL");
                    return;
                }
                HttpService httpService = new HttpService();
                Map<String, String> response = httpService.sendRequest(
                    url,
                    methodComboBox.getSelectedItem().toString(),
                    requestHeaders.getText(),
                    requestBody.getText()
                );
                setResponseHeaders(response.get("headers"));
                setResponseBody(response.get("body"));
            } catch (Exception ex) {
                showError("Request failed: " + ex.getMessage());
            }
        });

        panel.add(requestPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Response"));

        responseHeaders = new JBTextArea(5, 50);
        responseHeaders.setEditable(false);
        responseBody = new JBTextArea(10, 50);
        responseBody.setEditable(false);

        panel.add(new JBLabel("Response Headers:"), BorderLayout.NORTH);
        panel.add(new JBScrollPane(responseHeaders), BorderLayout.CENTER);
        panel.add(new JBLabel("Response Body:"), BorderLayout.SOUTH);
        panel.add(new JBScrollPane(responseBody), BorderLayout.CENTER);

        return panel;
    }

    private void updateEndpointsList() {
        endpointsModel.clear();
        List<Endpoint> endpoints = swaggerService.getEndpoints();
        for (Endpoint endpoint : endpoints) {
            endpointsModel.addElement(endpoint.toString());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void setMethod(String method) {
        ComboBox<String> methodComboBox = (ComboBox<String>) ((JPanel) getComponent(1))
            .getComponent(0).getComponent(3);
        methodComboBox.setSelectedItem(method);
    }

    private void setUrl(String url) {
        JBTextField urlField = (JBTextField) ((JPanel) getComponent(1))
            .getComponent(0).getComponent(1);
        urlField.setText(url);
    }

    public String getUrl() {
        return ((JBTextField) ((JPanel) getComponent(1))
            .getComponent(0).getComponent(1)).getText();
    }

    public String getMethod() {
        return ((ComboBox<String>) ((JPanel) getComponent(1))
            .getComponent(0).getComponent(3)).getSelectedItem().toString();
    }

    public String getRequestHeaders() {
        return requestHeaders.getText();
    }

    public String getRequestBody() {
        return requestBody.getText();
    }

    public void setResponseHeaders(String headers) {
        responseHeaders.setText(headers);
    }

    public void setResponseBody(String body) {
        responseBody.setText(body);
    }
}