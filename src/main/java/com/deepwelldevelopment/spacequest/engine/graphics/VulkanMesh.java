package com.deepwelldevelopment.spacequest.engine.graphics;

import com.deepwelldevelopment.spacequest.engine.graphics.vk.VulkanBuffer;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.VulkanContext;

public record VulkanMesh(String id, VulkanBuffer verticesBuffer, VulkanBuffer indicesBuffer, int numIndices,
        String materialId) {

    public void cleanup(VulkanContext context) {
        verticesBuffer.cleanup(context);
        indicesBuffer.cleanup(context);
    }
}
