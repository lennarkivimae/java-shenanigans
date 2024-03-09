package main.http;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Router {
    public static Map<String, Map<HttpMethod, Method>> routes = new HashMap<>();
}
