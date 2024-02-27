package http.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import http.HttpMethod;
import http.MediaType;
import http.Request;
import http.annotations.Route;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HelloController {
    @Route("/test")
    public static void index(Request request) throws IOException {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello World!");

        ObjectMapper objectMapper = new ObjectMapper();

        request.setResponseType(MediaType.JSON.value());
        request.response(objectMapper.writeValueAsString(response));
    }

    @Route("/test/{id:int}")
    public static void user(Request request, int id) {
        System.out.println("User method");
        System.out.println("ID: " + id);
    }

    @Route("/test2")
    public static void index2() {
        // Logic for handling "/hello" request
    }

    @Route(value = "/test", method = HttpMethod.POST)
    public static void hello() {
        // Logic for handling "/hello" request
    }
}
