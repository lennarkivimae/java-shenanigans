package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import http.HttpMethod;
import http.Request;
import utils.Utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import static http.Router.routes;

public class RequestHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        HttpMethod requestMethod = HttpMethod.valueOf(exchange.getRequestMethod());

        // Leading slash causes empty array item, remove leading slash
        String[] requestPathSegments = path.substring(1).split("/");
        boolean foundRoute = false;
        Method method = null;
        List<Object> args = new ArrayList<>();
        Request request = new Request(exchange);
        args.add(request);

        for (Map.Entry<String, Map<HttpMethod, Method>> routeSet: routes.entrySet()) {
            String route = routeSet.getKey();
            Map<HttpMethod, Method> methodMap = routeSet.getValue();

            String[] routePathSegments = route.substring(1).split("/");
            boolean hasSufficientSegments = routePathSegments.length == requestPathSegments.length;
            boolean matchesRouteSegment = Objects.equals(routePathSegments[0], requestPathSegments[0]);

            if (!hasSufficientSegments || !matchesRouteSegment) {
                continue;
            }

            Map<String, Object> routeWithArgs = getRouteWithArgs(routePathSegments, requestPathSegments);
            foundRoute = (boolean) routeWithArgs.get("foundRoute");

            if (foundRoute) {
                method = methodMap.get(requestMethod);
                args.addAll((Collection<?>) routeWithArgs.get("args"));

                break;
            }
        }


        if (foundRoute && method != null) {
            try {
                // TODO: Find and execute middlewares
                method.invoke(null, args.toArray());
            } catch (Exception e) {
                // TODO: Handle 500
                e.printStackTrace();
            }
        }

        // TODO: Handle 404
    }

    private Map<String, Object> getRouteWithArgs(String[] routePathSegments, String[] requestPathSegments) {
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

        boolean foundRoute = matchedSegments == routePathSegments.length;
        Map<String, Object> results = new HashMap<>();
        results.put("foundRoute", foundRoute);
        results.put("args", args);

        return results;
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
