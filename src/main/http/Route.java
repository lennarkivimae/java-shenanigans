package main.http;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static main.http.Router.routes;

public class Route {
    public static void register(Class<?> controllerClass) {
        Method[] methods = controllerClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(main.http.annotations.Route.class)) {
                main.http.annotations.Route annotation = method.getAnnotation(main.http.annotations.Route.class);
                String path = annotation.value();

                Map<HttpMethod, Method> currentRoute = findOrCreate(path);
                currentRoute.put(annotation.method(), method);

                routes.put(path, currentRoute);
            }
        }
    }

    private static Map<HttpMethod, Method> findOrCreate(String path) {
        routes.computeIfAbsent(path, k -> new HashMap<>());

        return routes.get(path);
    }
}
