package main.http.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.http.MediaType;
import main.http.Request;
import main.http.annotations.Route;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IndexController {
    @Route("/api/")
    public static void index(Request request) throws IOException {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello World!");

        ObjectMapper objectMapper = new ObjectMapper();

        String responseJson = objectMapper.writeValueAsString(response);
        String responseType = MediaType.JSON.value();

        request.setResponseType(responseType).response(responseJson);
    }
}
