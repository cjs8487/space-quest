package com.deepwelldevelopment.spacequest.engine.graph;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.system.MemoryUtil;

import com.deepwelldevelopment.spacequest.engine.graph.vk.CommandBuffer;
import com.deepwelldevelopment.spacequest.engine.graph.vk.CommandPool;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Queue;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanBuffer;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanContext;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanUtils;
import com.deepwelldevelopment.spacequest.engine.model.MeshData;
import com.deepwelldevelopment.spacequest.engine.model.ModelData;

public class ModelsCache {
    private final Map<String, VulkanModel> modelsMap;

    public ModelsCache() {
        this.modelsMap = new java.util.HashMap<>();
    }

    private static TransferBuffer createIndicesBuffers(VulkanContext context, MeshData meshData) {
        int[] indices = meshData.indices();
        int numIndices = indices.length;
        int bufferSize = numIndices * VulkanUtils.INT_SIZE;

        var srcBuffer = new VulkanBuffer(context, bufferSize,
                VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        var dstBuffer = new VulkanBuffer(context, bufferSize,
                VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);

        long mappedMemory = srcBuffer.map(context);
        IntBuffer data = MemoryUtil.memIntBuffer(mappedMemory, (int) srcBuffer.getRequestedSize());
        data.put(indices);
        srcBuffer.unmap(context);

        return new TransferBuffer(srcBuffer, dstBuffer);
    }

    private static TransferBuffer createVerticesBuffers(VulkanContext context, MeshData meshData) {
        float[] positions = meshData.positions();
        float[] texCoords = meshData.texCoords();
        if (texCoords == null || texCoords.length == 0) {
            texCoords = new float[(positions.length / 3) * 2];
        }
        int numElements = positions.length + texCoords.length;
        int bufferSize = numElements * VulkanUtils.FLOAT_SIZE;

        var srcBuffer = new VulkanBuffer(context, bufferSize,
                VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        var dstBuffer = new VulkanBuffer(context, bufferSize,
                VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);

        long mappedMemory = srcBuffer.map(context);
        FloatBuffer data = MemoryUtil.memFloatBuffer(mappedMemory, (int) srcBuffer.getRequestedSize());

        int rows = positions.length / 3;
        for (int row = 0; row < rows; row++) {
            int startPos = row * 3;
            int startTex = row * 2;
            data.put(positions[startPos]);
            data.put(positions[startPos + 1]);
            data.put(positions[startPos + 2]);
            data.put(texCoords[startTex]);
            data.put(texCoords[startTex + 1]);
        }

        srcBuffer.unmap(context);

        return new TransferBuffer(srcBuffer, dstBuffer);
    }

    public void loadModels(VulkanContext context, List<ModelData> models, CommandPool commandPool, Queue queue) {
        List<VulkanBuffer> stagingBufferList = new ArrayList<>();

        var cmd = new CommandBuffer(context, commandPool, true, true);
        cmd.beginRecording();

        for (ModelData modelData : models) {
            VulkanModel vkModel = new VulkanModel(modelData.id());
            modelsMap.put(vkModel.getId(), vkModel);

            for (MeshData meshData : modelData.meshes()) {
                TransferBuffer verticesBuffers = createVerticesBuffers(context, meshData);
                TransferBuffer indicesBuffers = createIndicesBuffers(context, meshData);
                stagingBufferList.add(verticesBuffers.srcBuffer());
                stagingBufferList.add(indicesBuffers.srcBuffer());
                verticesBuffers.recordTransferCommand(cmd);
                indicesBuffers.recordTransferCommand(cmd);

                VulkanMesh vkMesh = new VulkanMesh(meshData.id(), verticesBuffers.dstBuffer(),
                        indicesBuffers.dstBuffer(), meshData.indices().length);
                vkModel.getMeshes().add(vkMesh);
            }
        }

        cmd.endRecording();
        cmd.submitAndWait(context, queue);
        cmd.cleanup(context, commandPool);

        stagingBufferList.forEach(b -> b.cleanup(context));
    }

    public void cleanup(VulkanContext context) {
        modelsMap.forEach((k, t) -> t.cleanup(context));
        modelsMap.clear();
    }

    public VulkanModel getModel(String modelName) {
        return modelsMap.get(modelName);
    }

    public Map<String, VulkanModel> getModelsMap() {
        return modelsMap;
    }
}
