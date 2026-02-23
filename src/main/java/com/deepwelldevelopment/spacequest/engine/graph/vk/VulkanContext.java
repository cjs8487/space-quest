package com.deepwelldevelopment.spacequest.engine.graph.vk;

import com.deepwelldevelopment.spacequest.engine.EngineConfig;

public class VulkanContext {
    private final Instance instance;

    public VulkanContext() {
        var engCfg = EngineConfig.getInstance();
        this.instance = new Instance(engCfg.isVkValidate());
    }

    public void cleanup() {
        instance.cleanup();
    }
}
