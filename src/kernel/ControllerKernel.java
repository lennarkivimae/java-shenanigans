package kernel;

import http.Route;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
        try {
            Set<String> resources = new HashSet<>(java.util.Arrays.asList(
                    new File(classLoader.getResource(path).toURI()).list()
            ));

            return resources.stream()
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
