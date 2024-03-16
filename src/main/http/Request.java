package main.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class Request {
    HttpExchange exchange;
    private HttpResponseCodes code = HttpResponseCodes.OK;
    private String responseType = MediaType.HTML.value();
    private final OutputStream stream;

    private String characterEncoding = "UTF-8";
    private String templatesFolder = "templates";
    private String resourcesFolder = "resources";
    private String rootFolder = "src/main";

    public Request(HttpExchange exchange) {
        this.exchange = exchange;
        this.stream = exchange.getResponseBody();
    }

    public void setHeader(String name, String value) {
        exchange.getResponseHeaders().add(name, value);
    }

    public List<String> getHeader(String name) {
        return exchange.getRequestHeaders().get(name);
    }

    public String get(String name) {
        Map<String, String> params = this.getRequestParams();

        return params.get(name);
    }

    public Request setResponseCode(HttpResponseCodes code) {
        this.code = code;

        return this;
    }

    public Request setResponseType(String type) {
        this.responseType = type;

        return this;
    }

    public void render(String path, Map<String, Object> data) {
        TemplateEngine engine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setCharacterEncoding(this.characterEncoding);
        resolver.setPrefix("/" + this.templatesFolder + "/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);

        engine.setTemplateResolver(resolver);
        this.setResponseType(MediaType.HTML.value());

        try {
            File file = new File(String.format("%s/%s/%s/%s.html", this.rootFolder, this.resourcesFolder, this.templatesFolder, path));
            if (!file.exists() && !file.isDirectory()) {
                throw new IOException("File not found: " + path);
            }

            Context ctx = new Context();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                ctx.setVariable(entry.getKey(), entry.getValue());
            }

            this.response(engine.process(path, ctx), this.code);
        } catch (IOException e) {
            System.out.println("Request: Render: Failed to render file: " + e);

            try {
                Context ctx = new Context();

                this.response(engine.process("internal-error", ctx), HttpResponseCodes.INTERNAL_SERVER_ERROR);
            } catch (IOException ex) {
                System.out.println("Request: Render: Failed to render internal-error: " + ex);
                this.exchange.close();
            }
        }
    }

    public void response(String body, HttpResponseCodes code) throws IOException {
        int contentLength = body.getBytes().length;
        this.setHeader("Content-Length", String.valueOf(contentLength));

        try {
            this.exchange.sendResponseHeaders(code.getCode(), contentLength);

            if (responseType.equals(MediaType.JSON.value())) {
                respondJson(body);

                return;
            }

            this.setHeader("Content-Type", MediaType.HTML.getContentType());
            this.stream.write(body.getBytes());
        } catch (IOException e) {
            System.out.println("Request: Response: Failed to send response: " + e);
        } finally {
            // Free up connection.
            this.exchange.close();
        }
    }

    private void respondJson(String body) throws IOException {
        this.setHeader("Content-Type", MediaType.JSON.getContentType());
        this.stream.write(body.getBytes());
    }

    public Map<String, String> getRequestParams() {
        String paramString = exchange.getRequestURI().getQuery();
        Map<String, String> params = new HashMap<>();

        if (paramString != null) {
            for (String param : paramString.split("&")) {
                String[] parts = param.split("=");

                if (parts.length > 1) {
                    params.put(parts[0], parts[1]);
                } else {
                    params.put(parts[0], "");
                }
            }
        }

        return params;
    }

    public Set<Map.Entry<String, List<String>>> getHeaderList() {
        return exchange.getResponseHeaders().entrySet();
    }
}
