package handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;

public class HelloHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");

        HashMap<String, String> payload = new HashMap<>();
        payload.put("message", "success");

        String payloadJson = new ObjectMapper().writeValueAsString(payload);

        exchange.getResponseHeaders().putAll(headers);
        exchange.sendResponseHeaders(200, payloadJson.length());
        exchange.getResponseBody().write(payloadJson.getBytes());
    }
}
