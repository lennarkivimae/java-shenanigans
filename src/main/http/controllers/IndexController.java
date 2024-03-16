package main.http.controllers;

import main.http.HttpMethod;
import main.http.Request;
import main.http.annotations.Middleware;
import main.http.annotations.Route;
import main.http.middlewares.Logger;

import java.util.Collections;

public class IndexController {
    @Route("/user/{id:int}")
    @Middleware(Logger.class)
    public static void user(Request request, int id) {
        System.out.println("User method");
        System.out.println("ID: " + id);
    }

    @Route(value = "/test", method = HttpMethod.POST)
    public static void hello() {
        // Logic for handling "/hello" request
    }

    @Route("/")
    public static void index(Request request) {
        request.render("index", Collections.emptyMap());
    }
}
