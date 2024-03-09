package main.utils;

public class Utils {
    public static <T> T coalesce(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
