package com.sklcc.fpp.nets.settings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ReadSetting {
    public void init() {

         InputStream inputStream = this.getClass().getClassLoader()
         .getResourceAsStream("PortSetting.properties");

        try {
//            FileInputStream inputStream = new FileInputStream(
//                    "PortSetting.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            Settings.androidPort = Integer.valueOf(properties
                    .getProperty("androidPort"));
            Settings.nodePort = Integer.valueOf(properties
                    .getProperty("nodePort"));
            Settings.pcPort = Integer.valueOf(properties.getProperty("pcPort"));
            Settings.phpPort = Integer.valueOf(properties
                    .getProperty("phpPort"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
