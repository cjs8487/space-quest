package com.deepwelldevelopment.spacequest.engine.graph;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
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
import com.deepwelldevelopment.spacequest.engine.model.ModelDataRegistry;
import com.deepwelldevelopment.spacequest.engine.model.ProgrammaticModel;

public class ModelsCache {
    private final Map<String, VulkanModel> modelsMap;

    public ModelsCache() {
        this.modelsMap = new java.util.HashMap<>();
    }

    private static TransferBuffer createIndicesBuffers(VulkanContext context, MeshData meshData,
            DataInputStream idxInput) throws IOException {
        int bufferSize = meshData.indexSize();
        var srcBuffer = new VulkanBuffer(context, bufferSize,
                VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        var dstBuffer = new VulkanBuffer(context, bufferSize,
                VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);

        long mappedMemory = srcBuffer.map(context);
        IntBuffer data = MemoryUtil.memIntBuffer(mappedMemory, (int) srcBuffer.getRequestedSize());

        int valuesToRead = meshData.indexSize() / VulkanUtils.INT_SIZE;
        while (valuesToRead > 0) {
            data.put(idxInput.readInt());
            valuesToRead--;
        }

        srcBuffer.unmap(context);

        return new TransferBuffer(srcBuffer, dstBuffer);
    }

    private static TransferBuffer createVerticesBuffers(VulkanContext context, MeshData meshData,
            DataInputStream vtxInput) throws IOException {
        int bufferSize = meshData.vertexSize();
        var srcBuffer = new VulkanBuffer(context, bufferSize,
                VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        var dstBuffer = new VulkanBuffer(context, bufferSize,
                VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);

        long mappedMemory = srcBuffer.map(context);
        FloatBuffer data = MemoryUtil.memFloatBuffer(mappedMemory, (int) srcBuffer.getRequestedSize());

        int valuesToRead = meshData.vertexSize() / VulkanUtils.FLOAT_SIZE;
        while (valuesToRead > 0) {
            data.put(vtxInput.readFloat());
            valuesToRead--;
        }

        srcBuffer.unmap(context);

        return new TransferBuffer(srcBuffer, dstBuffer);
    }

    public void loadModels(VulkanContext context, List<ModelData> models, CommandPool commandPool, Queue queue) {
        try {
            List<VulkanBuffer> stagingBufferList = new ArrayList<>();

            var cmd = new CommandBuffer(context, commandPool, true, true);
            cmd.beginRecording();

            for (ModelData modelData : models) {
                VulkanModel vulkanModel = new VulkanModel(modelData.id());
                modelsMap.put(vulkanModel.getId(), vulkanModel);

                // Check if this is a programmatic model
                boolean isProgrammatic = modelData.vertexPath().startsWith("memory://");

                DataInputStream vtxInput;
                DataInputStream idxInput;

                if (isProgrammatic) {
                    String modelId = modelData.id();
                    vtxInput = ModelDataRegistry.getVertexInputStream(modelId);
                    idxInput = ModelDataRegistry.getIndexInputStream(modelId);
                } else {
                    vtxInput = new DataInputStream(
                            new BufferedInputStream(new FileInputStream(modelData.vertexPath())));
                    idxInput = new DataInputStream(
                            new BufferedInputStream(new FileInputStream(modelData.indexPath())));
                }

                // Transform meshes loading their data into GPU buffers
                for (MeshData meshData : modelData.meshes()) {
                    TransferBuffer verticesBuffers = createVerticesBuffers(context, meshData, vtxInput);
                    TransferBuffer indicesBuffers = createIndicesBuffers(context, meshData, idxInput);
                    stagingBufferList.add(verticesBuffers.srcBuffer());
                    stagingBufferList.add(indicesBuffers.srcBuffer());
                    verticesBuffers.recordTransferCommand(cmd);
                    indicesBuffers.recordTransferCommand(cmd);

                    VulkanMesh vulkanMesh = new VulkanMesh(meshData.id(), verticesBuffers.dstBuffer(),
                            indicesBuffers.dstBuffer(), meshData.indexSize() / VulkanUtils.INT_SIZE,
                            meshData.materialId());
                    vulkanModel.getMeshes().add(vulkanMesh);
                }

                // Close streams if they're file-based
                if (!isProgrammatic) {
                    vtxInput.close();
                    idxInput.close();
                }
            }

            cmd.endRecording();
            cmd.submitAndWait(context, queue);
            cmd.cleanup(context, commandPool);

            stagingBufferList.forEach(b -> b.cleanup(context));
        } catch (Exception excp) {
            throw new RuntimeException(excp);
        }
    }

    public void loadProgrammaticModel(VulkanContext context, ProgrammaticModel programmaticModel,
            CommandPool commandPool, Queue queue) {
        ModelData modelData = programmaticModel.toModelData();
        loadModels(context, List.of(modelData), commandPool, queue);
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
