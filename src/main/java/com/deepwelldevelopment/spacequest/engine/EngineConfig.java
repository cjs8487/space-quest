package com.deepwelldevelopment.spacequest.engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.tinylog.Logger;

public class EngineConfig {

    private static final int DEFAULT_UPS = 30;
    private static final String FILENAME = "eng.properties";
    private static EngineConfig instance;
    private int ups;

    private EngineConfig() {
        var props = new Properties();

        try (InputStream is = EngineConfig.class.getResourceAsStream("/" + FILENAME)) {
            props.load(is);
            ups = Integer.parseInt(props.getOrDefault("ups", DEFAULT_UPS).toString());
        } catch (IOException e) {
            Logger.error("Could not read [{}] properties file", FILENAME, e);
        }
    }

    public static EngineConfig getInstance() {
        if (instance == null) {
            instance = new EngineConfig();
        }
        return instance;
    }

    public int getUps() {
        return ups;
    }
}
