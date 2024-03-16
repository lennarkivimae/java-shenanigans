package main.http.middlewares;

import main.http.Request;

public class Logger extends Middleware {
    @Override
    public void invoke(Request request) {
        System.out.println("Logger middleware");
    }
}
