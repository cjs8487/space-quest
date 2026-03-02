package com.deepwelldevelopment.spacequest.engine.graphics.vk;

import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;

public class EmptyVertexBufferStruct {
    private final VkPipelineVertexInputStateCreateInfo vi;

    public EmptyVertexBufferStruct() {
        vi = VkPipelineVertexInputStateCreateInfo.calloc();
        vi.sType$Default();
    }

    public void cleanup() {
        vi.free();
    }

    public VkPipelineVertexInputStateCreateInfo getVi() {
        return vi;
    }
}
