package com.example.orchestrator.utility;

import java.io.InputStream;

public class ResourceLoader {
    public static InputStream load(String resourceName) {
        return ResourceLoader.class.getClassLoader().getResourceAsStream(resourceName);
    }
}

