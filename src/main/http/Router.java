package main.http;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Router {
    public static Map<String, Route> getRoutes = new HashMap<>();
    public static Map<String, Route> postRoutes = new HashMap<>();
    public static Map<String, Route> putRoutes = new HashMap<>();
    public static Map<String, Route> deleteRoutes = new HashMap<>();

    public static void addRoute(HttpMethod httpMethod, String path, Route route) {
        if (httpMethod.equals(HttpMethod.GET)) {
            getRoutes.put(path, route);
        } else if (httpMethod.equals(HttpMethod.POST)) {
            postRoutes.put(path, route);
        } else if (httpMethod.equals(HttpMethod.PUT)) {
            putRoutes.put(path, route);
        } else if (httpMethod.equals(HttpMethod.DELETE)) {
            deleteRoutes.put(path, route);
        }
    }

    public static Map<String, Route> getRoutesForHttpMethod(HttpMethod httpMethod) {
        if (httpMethod.equals(HttpMethod.GET)) {
            return getRoutes;
        } else if (httpMethod.equals(HttpMethod.POST)) {
            return postRoutes;
        } else if (httpMethod.equals(HttpMethod.PUT)) {
            return putRoutes;
        } else if (httpMethod.equals(HttpMethod.DELETE)) {
            return deleteRoutes;
        }

        return new HashMap<>();
    }
}
