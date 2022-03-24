package com.epam.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

@Slf4j
public class PropertiesUtil {
    private final Path propertiesFilePath;
    private final Properties properties;

    public PropertiesUtil(Path propertiesFilePath) {
        this.propertiesFilePath = propertiesFilePath;
        this.properties = new Properties();
    }

    public String readProperties(String key) throws IOException {
        try (InputStream is = new FileInputStream(propertiesFilePath.toString())) {
            properties.load(is);
        }
        return properties.getProperty(key);
    }
}
