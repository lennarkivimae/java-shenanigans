package main.http;

import main.http.annotations.Middleware;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Route {
    private final Method controllerMethod;
    private final String path;
    private final HttpMethod httpMethod;

    private final List<Class<? extends main.http.middlewares.Middleware>> middlewares;

    public Route(Method controllerMethod, String path) {
        this.controllerMethod = controllerMethod;
        this.path = path;
        this.middlewares = new ArrayList<>();
        this.httpMethod = this.controllerMethod.getAnnotation(main.http.annotations.Route.class).method();
    }

    public String getPath() {
        return path;
    }

    public static void register(Class<?> controllerClass) {
        Method[] methods = controllerClass.getDeclaredMethods();

        for (Method method : methods) {
            if (!method.isAnnotationPresent(main.http.annotations.Route.class)) {
                continue;
            }

            main.http.annotations.Route annotation = method.getAnnotation(main.http.annotations.Route.class);
            String path = annotation.value();

            Route route = new Route(method, path);
            route.registerMiddlewaresToRoute();
            Router.addRoute(route.httpMethod, path, route);
        }
    }

    public Method getRouteControllerMethod() {
        return this.controllerMethod;
    }

    public List<Class<? extends main.http.middlewares.Middleware>> getMiddlewares() {
        return this.middlewares;
    }

    public void registerMiddlewaresToRoute() {
        Middleware[] middlewareAnnotations = this.controllerMethod.getAnnotationsByType(Middleware.class);

        for (Middleware middleware : middlewareAnnotations) {
            this.middlewares.add(middleware.value());
        }
    }
}
