package com.epam.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

    private static final String PROP_FILE = "/zephyr.properties";
    private final Properties properties = new Properties();

    public String read(String key) {
        try (InputStream is = getClass().getResourceAsStream(PROP_FILE)) {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(key);
    }
}
