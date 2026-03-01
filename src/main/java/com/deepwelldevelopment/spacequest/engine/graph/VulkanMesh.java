package com.deepwelldevelopment.spacequest.engine.graph;

import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanBuffer;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanContext;

public record VulkanMesh(String id, VulkanBuffer verticesBuffer, VulkanBuffer indicesBuffer, int numIndices,
        String materialId) {

    public void cleanup(VulkanContext context) {
        verticesBuffer.cleanup(context);
        indicesBuffer.cleanup(context);
    }
}
