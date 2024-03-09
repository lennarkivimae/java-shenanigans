package main.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.http.HttpMethod;
import main.http.Request;
import main.utils.Utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import static main.http.Router.routes;

public class RequestHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        HttpMethod requestMethod = HttpMethod.valueOf(exchange.getRequestMethod());
        MethodWithArguments methodWithArguments = new MethodWithArguments(path, requestMethod);
        Method method = methodWithArguments.method;

        List<Object> arguments = new ArrayList<>();
        Request request = new Request(exchange);
        arguments.add(request);
        arguments.addAll(methodWithArguments.arguments);

        if (method != null) {
            try {
                // TODO: Find and execute middlewares
                method.invoke(null, arguments.toArray());
            } catch (Exception e) {
                // TODO: Handle 500
                e.printStackTrace();
            }
        }

        // TODO: Handle 404
    }
}

class MethodWithArguments {
    public Method method;
    public List<Object> arguments;

    MethodWithArguments(String path, HttpMethod requestMethod) {
        this.method = null;
        this.arguments = null;

        this.init(path, requestMethod);
    }

    private void init(String path, HttpMethod requestMethod) {
        // Leading slash causes empty array item, remove leading slash
        String[] requestPathSegments = path.substring(1).split("/");

        for (Map.Entry<String, Map<HttpMethod, Method>> routeSet: routes.entrySet()) {
            String route = routeSet.getKey();
            Map<HttpMethod, Method> methodMap = routeSet.getValue();

            String[] routePathSegments = route.substring(1).split("/");
            boolean hasSufficientSegments = routePathSegments.length == requestPathSegments.length;
            boolean matchesRouteSegment = Objects.equals(routePathSegments[0], requestPathSegments[0]);

            if (!hasSufficientSegments || !matchesRouteSegment) {
                continue;
            }

            RouteWithArguments routeWithArgs = new RouteWithArguments(routePathSegments, requestPathSegments);

            if (routeWithArgs.found) {
                this.method = methodMap.get(requestMethod);
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
