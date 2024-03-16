package main.http.annotations;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Middlewares.class)
public @interface Middleware {
    Class<? extends main.http.middlewares.Middleware> value();
}
