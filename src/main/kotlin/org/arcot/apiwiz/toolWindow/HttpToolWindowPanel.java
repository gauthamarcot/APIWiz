package org.arcot.apiwiz.toolWindow;

import javax.swing.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.JBTextArea;
import org.arcot.apiwiz.services.HttpService;
import org.arcot.apiwiz.services.SwaggerService;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class HttpToolWindowPanel extends JPanel {

    private final Project project;
    private final JTextArea requestHeaders;
    private final JTextArea requestBody;
    private final JTextArea responseHeaders;
    private final JTextArea responseBody;

    public HttpToolWindowPanel(Project project) {
        super(new BorderLayout());
        this.project = project;

        JPanel panel = new JPanel(new VerticalFlowLayout());

        JBTextField urlField = new JBTextField();
        panel.add(new JLabel("URL:"));
        panel.add(urlField);

        ComboBox<String> methodComboBox = new ComboBox<>(new String[]{"GET", "POST", "PUT", "DELETE"});
        panel.add(new JLabel("Method:"));
        panel.add(methodComboBox);

        requestHeaders = new JBTextArea(5, 50);
        panel.add(new JLabel("Request Headers:"));
        panel.add(new JBScrollPane(requestHeaders));

        requestBody = new JBTextArea(10, 50);
        panel.add(new JLabel("Request Body:"));
        panel.add(new JBScrollPane(requestBody));

        JButton sendButton = new JButton("Send");
        panel.add(sendButton);

        responseHeaders = new JBTextArea(5, 50);
        responseHeaders.setEditable(false);
        panel.add(new JLabel("Response Headers:"));
        panel.add(new JBScrollPane(responseHeaders));

        responseBody = new JBTextArea(10, 50);
        responseBody.setEditable(false);
        panel.add(new JLabel("Response Body:"));
        panel.add(new JBScrollPane(responseBody));

        JBTextField swaggerUrlField = new JBTextField();
        panel.add(new JLabel("Swagger URL:"));
        panel.add(swaggerUrlField);

        JButton importSwaggerButton = new JButton("Import from Swagger");
        panel.add(importSwaggerButton);

        JBTextField flaskApiUrlField = new JBTextField();
        panel.add(new JLabel("Flask API URL:"));
        panel.add(flaskApiUrlField);

        JButton scanFlaskButton = new JButton("Scan Flask API");
        panel.add(scanFlaskButton);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    HttpService httpService = new HttpService();
                    Map<String, String> response = httpService.sendRequest(
                            getUrl(), getMethod(), getRequestHeaders(), getRequestBody());

                    setResponseHeaders(response.get("headers"));
                    setResponseBody(response.get("body"));
                } catch (Exception ex) {
                    setResponseHeaders("Error");
                    setResponseBody(ex.getMessage());
                }
            }
        });

        importSwaggerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    SwaggerService swaggerService = new SwaggerService();
                    String swaggerUrl = swaggerUrlField.getText();
                    swaggerService.importFromSwagger(swaggerUrl);
                } catch (Exception ex) {
                    setResponseHeaders("Error");
                    setResponseBody(ex.getMessage());
                }
            }
        });

        scanFlaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    SwaggerService swaggerService = new SwaggerService();
                    String flaskApiUrl = flaskApiUrlField.getText();
                    swaggerService.scanFlaskApi(flaskApiUrl);
                } catch (Exception ex) {
                    setResponseHeaders("Error");
                    setResponseBody(ex.getMessage());
                }
            }
        });

        add(panel, BorderLayout.CENTER);
    }

    // Getters for request fields
    public String getUrl() {
        return ((JBTextField) ((JPanel) getComponent(0)).getComponent(1)).getText();
    }

    public String getMethod() {
        return ((ComboBox<String>) ((JPanel) getComponent(0)).getComponent(3)).getSelectedItem().toString();
    }

    public String getRequestHeaders() {
        return requestHeaders.getText();
    }

    public String getRequestBody() {
        return requestBody.getText();
    }

    // Setters for response fields
    public void setResponseHeaders(String headers) {
        responseHeaders.setText(headers);
    }

    public void setResponseBody(String body) {
        responseBody.setText(body);
    }
}