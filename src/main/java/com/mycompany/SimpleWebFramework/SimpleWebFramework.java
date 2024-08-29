package com.mycompany.SimpleWebFramework;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class SimpleWebFramework {

    private static final Map<String, BiFunction<Request, Response, String>> GET_ROUTES = new HashMap<>();
    private static String staticFileRoot = "webroot";

    public static void get(String route, BiFunction<Request, Response, String> handler) {
        System.out.println("Registering route: " + route);
        GET_ROUTES.put(route, handler);
    }

    public static void staticfiles(String directory) {
        staticFileRoot = directory;
    }

    public static String handleGetRequest(String path, Request request, Response response) {
        BiFunction<Request, Response, String> handler = GET_ROUTES.get(path);
        if (handler != null) {
            return handler.apply(request, response);
        }
        return null;
    }

    public static String getStaticFileRoot() {
        return staticFileRoot;
    }
}
