package org.arcot.apiwiz.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpService {

    public Map<String, String> sendRequest(String url, String method, String headers, String body) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod(method);

        // Add headers
        if (!headers.isEmpty()) {
            String[] headersArray = headers.split("\n");
            for (String header : headersArray) {
                String[] keyValue = header.split(":");
                con.setRequestProperty(keyValue[0].trim(), keyValue[1].trim());
            }
        }

        // Send request body if needed
        if (!body.isEmpty() && (method.equals("POST") || method.equals("PUT"))) {
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(body.getBytes());
            os.flush();
            os.close();
        }

        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("status", String.valueOf(responseCode));
        responseMap.put("headers", con.getHeaderFields().toString());
        responseMap.put("body", response.toString());

        return responseMap;
    }
}