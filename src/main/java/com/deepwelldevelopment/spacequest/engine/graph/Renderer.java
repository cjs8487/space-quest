package com.deepwelldevelopment.spacequest.engine.graph;

import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanContext;
import com.deepwelldevelopment.spacequest.engine.EngineContext;

public class Renderer {

    private final VulkanContext vulkanContext;

    public Renderer(EngineContext context) {
        this.vulkanContext = new VulkanContext();
    }

    public void cleanup() {
        this.vulkanContext.cleanup();
    }

    public void render(EngineContext context) {
    }
}
