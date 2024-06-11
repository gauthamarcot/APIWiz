package org.arcot.apiwiz.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

public class SwaggerService {

    public void importFromSwagger(String swaggerUrl) throws Exception {
        URL url = new URL(swaggerUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(con.getInputStream());

        // Process the Swagger JSON to extract API endpoints
        JsonNode pathsNode = rootNode.get("paths");
        Iterator<Map.Entry<String, JsonNode>> fields = pathsNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String path = entry.getKey();
            JsonNode methodsNode = entry.getValue();
            methodsNode.fieldNames().forEachRemaining(method -> {
                // Add each endpoint to the namespace
                System.out.println(method.toUpperCase() + " " + path);
            });
        }
    }

    public void scanFlaskApi(String flaskApiUrl) throws Exception {
        URL url = new URL(flaskApiUrl + "/swagger.json");
        importFromSwagger(url.toString());
    }
}