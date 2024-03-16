package main.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.http.*;
import main.http.middlewares.Middleware;
import main.utils.Utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

public class RequestHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Request request = new Request(exchange);
        String path = exchange.getRequestURI().getPath();
        HttpMethod requestMethod = HttpMethod.valueOf(exchange.getRequestMethod());
        RouteData methodWithArguments = new RouteData(path, requestMethod);
        Method method = methodWithArguments.method;

        if (method == null) {
            request.setResponseCode(HttpResponseCodes.NOT_FOUND);
            request.render("not-found", Collections.emptyMap());

            return;
        }

        this.executeRoute(request, method, methodWithArguments);
    }

    private void executeRoute(Request request, Method method, RouteData routeData) {
        List<Object> arguments = new ArrayList<>();
        arguments.add(request);
        arguments.addAll(routeData.arguments);

        try {
            boolean satisfied = true;

            for (Class<? extends Middleware> middleware : routeData.middlewares) {
                try {
                    Middleware middlewareInstance = middleware.getConstructor().newInstance();
                    middlewareInstance.invoke(request);
                } catch (Exception e) {
                    satisfied = false;

                    break;
                }
            }

            if (satisfied) {
                method.invoke(null, arguments.toArray());
            }
        } catch (Exception e) {
            request.setResponseCode(HttpResponseCodes.INTERNAL_SERVER_ERROR);
            request.render("internal-error", Collections.emptyMap());
        }
    }
}

class RouteData {
    public Method method;
    public List<Object> arguments;
    public List<Class<? extends main.http.middlewares.Middleware>> middlewares;

    RouteData(String path, HttpMethod requestMethod) {
        this.method = null;
        this.arguments = null;

        this.init(path, requestMethod);
    }

    private void init(String path, HttpMethod requestMethod) {
        // Leading slash causes empty array item, remove leading slash
        String[] requestPathSegments = path.substring(1).split("/");

        Map<String, Route> routes = Router.getRoutesForHttpMethod(requestMethod);

        for (Map.Entry<String, Route> routeSet : routes.entrySet()) {
            String route = routeSet.getKey();
            String[] routePathSegments = route.substring(1).split("/");
            boolean hasSufficientSegments = routePathSegments.length == requestPathSegments.length;
            boolean matchesRouteSegment = Objects.equals(routePathSegments[0], requestPathSegments[0]);

            if (!hasSufficientSegments || !matchesRouteSegment) {
                continue;
            }

            RouteWithArguments routeWithArgs = new RouteWithArguments(routePathSegments, requestPathSegments);

            if (routeWithArgs.found) {
                this.method = routeSet.getValue().getRouteControllerMethod();
                this.middlewares = routeSet.getValue().getMiddlewares();
                this.arguments = routeWithArgs.arguments;
            }
        }
    }
}

class RouteWithArguments {
    public boolean found;
    public List<Object> arguments;

    RouteWithArguments(String[] routePathSegments, String[] requestPathSegments) {
        this.found = false;
        this.arguments = new ArrayList<>();

        this.init(routePathSegments, requestPathSegments);
    }

    private void init(String[] routePathSegments, String[] requestPathSegments) {
        int matchedSegments = 0;
        List<Object> args = new ArrayList<>();

        for (int i = 0; i < routePathSegments.length; i++) {
            String routePathSegment = routePathSegments[i];

            if (routePathSegment.contains("{") && routePathSegment.contains("}")) {
                matchedSegments++;
                String requestPathSegment = requestPathSegments[i];
                args.add(this.getArgument(routePathSegment, requestPathSegment));

                continue;
            }

            if (requestPathSegments[i].equals(routePathSegment)) {
                matchedSegments++;
            }
        }

        // Route is found when segments match and are in equal length
        this.found = matchedSegments == routePathSegments.length;
        this.arguments = args;
    }

    private Object getArgument(String routePathSegment, String requestPathSegment) {
        String argumentWithoutBrackets = routePathSegment.substring(1, routePathSegment.length() - 1);
        String[] argumentNameAndType = argumentWithoutBrackets.split(":");
        String argumentType = Utils.coalesce(argumentNameAndType[1], "String");

        if (argumentType.equals("int")) {
            return Integer.parseInt(requestPathSegment);
        }

        return requestPathSegment;
    }
}
