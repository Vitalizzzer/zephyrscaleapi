package com.epam;

import com.epam.client.ResultPublisher;
import com.epam.utils.FileUtil;
import com.epam.utils.PropertiesUtil;

public class Main {

    public static void main(String[] args){
        FileUtil fileUtil = new FileUtil();
        PropertiesUtil propertiesUtil = new PropertiesUtil();

        ResultPublisher publisher = new ResultPublisher(fileUtil, propertiesUtil);
            publisher.publishResult();
    }
}
