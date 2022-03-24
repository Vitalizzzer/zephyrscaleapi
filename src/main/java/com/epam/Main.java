package com.epam;

import com.epam.client.ResultPublisher;
import com.epam.utils.FileUtil;
import com.epam.utils.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

@Slf4j
public class Main {

    private static final String PROP_FILE = "zephyr.properties";

    public static void main(String[] args) {
        FileUtil fileUtil = new FileUtil();
        ResultPublisher publisher;

        try {
            Path resourceFilePath = fileUtil.findResourceFilePath(PROP_FILE);
            PropertiesUtil propertiesUtil = new PropertiesUtil(resourceFilePath);
            publisher = new ResultPublisher(fileUtil, propertiesUtil);
            publisher.publishResult();
        } catch (IOException | URISyntaxException e) {
            log.error("Failed to publish report to Zephyr Scale. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
