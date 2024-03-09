package main.kernel;

import main.http.Route;
import main.utils.Utils;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class ControllerKernel {
    public static void loadControllers(String packageName) {
        Set<Class<?>> classes = getClasses(packageName);
        for (Class<?> clazz : classes) {
            registerController(clazz);
        }
    }

    public static Set<Class<?>> getClasses(String packageName) {
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        if (classLoader == null || classLoader.getResource(path) == null) {
             return Collections.emptySet();
        }

        try {
            URI fileURI = Objects.requireNonNull(classLoader.getResource(path)).toURI();
            File file = new File(fileURI);
            String[] classList = Utils.coalesce(file.list(), new String[]{});

            return Arrays.stream(classList)
                    .filter(resource -> resource.endsWith(".class"))
                    .map(resource -> {
                        String className = resource.substring(0, resource.length() - 6);
                        try {
                            return Class.forName(packageName + '.' + className);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .collect(Collectors.toSet());

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }
    public static void registerController(Class<?> controllerClass) {
        Route.register(controllerClass);
    }
}
