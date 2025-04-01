package org.arcot.apiwiz.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class SwaggerService {
    private final List<Endpoint> endpoints = new ArrayList<>();

    public static class Endpoint {
        private final String method;
        private final String path;
        private final String description;

        public Endpoint(String method, String path, String description) {
            this.method = method;
            this.path = path;
            this.description = description;
        }

        public String getMethod() { return method; }
        public String getPath() { return path; }
        public String getDescription() { return description; }

        @Override
        public String toString() {
            return method + " " + path + " - " + description;
        }
    }

    public void importFromSwagger(String swaggerUrl) throws Exception {
        endpoints.clear();
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
                JsonNode methodNode = methodsNode.get(method);
                String description = methodNode.has("summary") ? 
                    methodNode.get("summary").asText() : 
                    methodNode.has("description") ? 
                        methodNode.get("description").asText() : 
                        "";
                endpoints.add(new Endpoint(method.toUpperCase(), path, description));
            });
        }
    }

    public void scanFlaskApi(String flaskApiUrl) throws Exception {
        URL url = new URL(flaskApiUrl + "/swagger.json");
        importFromSwagger(url.toString());
    }

    public List<Endpoint> getEndpoints() {
        return Collections.unmodifiableList(endpoints);
    }

    public void clearEndpoints() {
        endpoints.clear();
    }
}