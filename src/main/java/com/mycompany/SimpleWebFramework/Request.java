package com.mycompany.SimpleWebFramework;

import java.util.HashMap;
import java.util.Map;

public class Request {
    private final Map<String, String> parameters = new HashMap<>();

    public void setParameter(String key, String value) {
        parameters.put(key, value);
    }

    public String getValues(String key) {
        return parameters.getOrDefault(key, "");
    }
}