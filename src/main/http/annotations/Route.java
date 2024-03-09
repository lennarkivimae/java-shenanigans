package main.http.annotations;

import main.http.HttpMethod;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Route {
    String value();
    HttpMethod method() default HttpMethod.GET;
}
