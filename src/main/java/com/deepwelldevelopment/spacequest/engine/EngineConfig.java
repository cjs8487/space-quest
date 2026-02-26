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
    private boolean vkValidate;
    private String physicalDeviceName;
    private int requestedImages;
    private boolean vsync;
    private boolean debugShaders;
    private boolean shaderRecompilation;
    private float fov;
    private float zNear;
    private float zFar;
    private String defaultTexturePath;
    private int maxDescs;

    private EngineConfig() {
        var props = new Properties();

        try (InputStream is = EngineConfig.class.getResourceAsStream("/" + FILENAME)) {
            props.load(is);
            ups = Integer.parseInt(props.getOrDefault("ups", DEFAULT_UPS).toString());
            vkValidate = Boolean.parseBoolean(props.getOrDefault("vkValidate", "false").toString());
            physicalDeviceName = props.getOrDefault("physicalDeviceName", "").toString();
            requestedImages = Integer.parseInt(props.getOrDefault("requestedImages", "3").toString());
            vsync = Boolean.parseBoolean(props.getOrDefault("vsync", "false").toString());
            debugShaders = Boolean.parseBoolean(props.getOrDefault("debugShaders", "false").toString());
            shaderRecompilation = Boolean.parseBoolean(props.getOrDefault("shaderRecompilation", "false").toString());
            fov = Float.parseFloat(props.getOrDefault("fov", 60.0f).toString());
            zNear = Float.parseFloat(props.getOrDefault("zNear", 1.0f).toString());
            zFar = Float.parseFloat(props.getOrDefault("zFar", 100.0f).toString());
            defaultTexturePath = props.getProperty("defaultTexturePath").toString();
            maxDescs = Integer.parseInt(props.getOrDefault("maxDescs", 1000).toString());
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

    public boolean isVkValidate() {
        return vkValidate;
    }

    public String getPhysicalDeviceName() {
        return physicalDeviceName;
    }

    public int getRequestedImages() {
        return requestedImages;
    }

    public boolean getVSync() {
        return vsync;
    }

    public boolean isDebugShaders() {
        return debugShaders;
    }

    public boolean isShaderRecompilation() {
        return shaderRecompilation;
    }

    public float getFov() {
        return fov;
    }

    public float getZNear() {
        return zNear;
    }

    public float getZFar() {
        return zFar;
    }

    public String getDefaultTexturePath() {
        return defaultTexturePath;
    }

    public int getMaxDescs() {
        return maxDescs;
    }
}
