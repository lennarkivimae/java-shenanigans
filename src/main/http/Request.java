package main.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Request {
    HttpExchange exchange;
    private int code = 200;
    private String responseType = MediaType.HTML.value();
    private final OutputStream stream;

    public Request(HttpExchange exchange) {
        this.exchange = exchange;
        this.stream = exchange.getResponseBody();
    }

    public void setHeader(String name, String value) {
        exchange.getResponseHeaders().add(name, value);
    }

    public List<String> getHeader(String name) {
        return exchange.getRequestHeaders().get(name);
    }

    public String get(String name) {
        Map<String, String> params = this.getRequestParams();

        return params.get(name);
    }

    public Request setResponseCode(int code) {
        this.code = code;

        return this;
    }

    public Request setResponseType(String type) {
        this.responseType = type;

        return this;
    }

    public void render(String path) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String str = "";

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            while ((str = reader.readLine()) != null) {
                stringBuilder.append(str);
            }

            reader.close();

            this.response(stringBuilder.toString());
        } catch (IOException e) {
            System.out.println("Request: Render: Failed to render file: " + path);
        }
    }

    public void response(String body) throws IOException {
        int contentLength = body.getBytes().length;
        this.setHeader("Content-Length", String.valueOf(contentLength));

        try {
            this.exchange.sendResponseHeaders(code, contentLength);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (responseType.equals(MediaType.JSON.value())) {
            respondJson(body);

            return;
        }

        this.stream.write(body.getBytes());
    }

    private void respondJson(String body) throws IOException {
        this.setHeader("Content-Type",  MediaType.JSON.getContentType());

        this.stream.write(body.getBytes());
    }

    public Map<String, String> getRequestParams() {
        String paramString = exchange.getRequestURI().getQuery();
        Map<String, String> params = new HashMap<>();

        if (paramString != null) {
            for (String param : paramString.split("&")) {
                String[] parts = param.split("=");

                if (parts.length > 1) {
                    params.put(parts[0], parts[1]);
                } else {
                    params.put(parts[0], "");
                }
            }
        }

        return params;
    }

    public Set<Map.Entry<String, List<String>>> getHeaderList() {
        return exchange.getResponseHeaders().entrySet();
    }
}
