package main.http.middlewares;

import main.http.Request;

public abstract class Middleware {
    public abstract void invoke(Request request);
}
