package org.arcot.apiwiz.toolWindow;

public class RequestNode {
    private String name;
    private String method;
    private String url;
    private String headers;
    private String body;

    public RequestNode(String name, String method, String url) {
        this.name = name;
        this.method = method;
        this.url = url;
        this.headers = "";
        this.body = "";
    }

    public String getName() { return name; }
    public String getMethod() { return method; }
    public String getUrl() { return url; }
    public String getHeaders() { return headers; }
    public String getBody() { return body; }

    public void setHeaders(String headers) { this.headers = headers; }
    public void setBody(String body) { this.body = body; }

    @Override
    public String toString() {
        return name;
    }
} 