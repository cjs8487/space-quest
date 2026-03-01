package com.deepwelldevelopment.spacequest.engine.graph;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;

import java.nio.ByteBuffer;
import java.util.List;

import org.lwjgl.system.MemoryUtil;
import org.tinylog.Logger;

import com.deepwelldevelopment.spacequest.engine.graph.vk.CommandBuffer;
import com.deepwelldevelopment.spacequest.engine.graph.vk.CommandPool;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Queue;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Texture;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanBuffer;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanContext;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanUtils;
import com.deepwelldevelopment.spacequest.engine.model.MaterialData;

public class MaterialsCache {

    private static final int MATERIAL_SIZE = VulkanUtils.VEC4_SIZE + VulkanUtils.VEC2_SIZE * 2
            + VulkanUtils.INT_SIZE * 4;
    private final IndexedLinkedHashMap<String, VulkanMaterial> materialsMap;
    private VulkanBuffer materialsBuffer;

    public MaterialsCache() {
        materialsMap = new IndexedLinkedHashMap<>();
    }

    public void cleanup(VulkanContext vkCtx) {
        if (materialsBuffer != null) {
            materialsBuffer.cleanup(vkCtx);
        }
    }

    public VulkanMaterial getMaterial(String id) {
        return materialsMap.get(id);
    }

    public VulkanBuffer getMaterialsBuffer() {
        return materialsBuffer;
    }

    public int getPosition(String id) {
        int result = -1;
        if (id != null) {
            result = materialsMap.getIndexOf(id);
        } else {
            Logger.warn("Could not find material with id [{}]", id);
        }
        return result;
    }

    public void loadMaterials(VulkanContext vkCtx, List<MaterialData> materials, TextureCache textureCache,
            CommandPool cmdPool, Queue queue) {
        int numMaterials = materials.size();
        int bufferSize = MATERIAL_SIZE * numMaterials;

        var srcBuffer = new VulkanBuffer(vkCtx, bufferSize,
                VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        materialsBuffer = new VulkanBuffer(vkCtx, bufferSize,
                VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);

        var cmd = new CommandBuffer(vkCtx, cmdPool, true, true);
        cmd.beginRecording();

        TransferBuffer transferBuffer = new TransferBuffer(srcBuffer, materialsBuffer);
        long mappedMemory = srcBuffer.map(vkCtx);
        ByteBuffer data = MemoryUtil.memByteBuffer(mappedMemory, (int) srcBuffer.getRequestedSize());

        int offset = 0;
        for (int i = 0; i < numMaterials; i++) {
            var material = materials.get(i);
            String texturePath = material.texturePath();
            boolean hasTexture = texturePath != null && !texturePath.isEmpty();
            boolean isTransparent;
            if (hasTexture) {
                Texture texture = textureCache.addTexture(vkCtx, texturePath, texturePath, VK_FORMAT_R8G8B8A8_SRGB);
                isTransparent = texture.isTransparent();
            } else {
                isTransparent = material.diffuseColor().w < 1.0f;
            }
            VulkanMaterial vulkanMaterial = new VulkanMaterial(material.id(), isTransparent);
            materialsMap.put(vulkanMaterial.id(), vulkanMaterial);

            material.diffuseColor().get(offset, data);
            data.putFloat(offset + VulkanUtils.VEC4_SIZE, material.uvScale().x);
            data.putFloat(offset + VulkanUtils.VEC4_SIZE + VulkanUtils.FLOAT_SIZE, material.uvScale().y);
            data.putFloat(offset + VulkanUtils.VEC4_SIZE + VulkanUtils.VEC2_SIZE, material.uvOffset().x);
            data.putFloat(offset + VulkanUtils.VEC4_SIZE + VulkanUtils.VEC2_SIZE + VulkanUtils.FLOAT_SIZE,
                    material.uvOffset().y);
            data.putInt(offset + VulkanUtils.VEC4_SIZE + VulkanUtils.VEC2_SIZE * 2, hasTexture ? 1 : 0);
            data.putInt(offset + VulkanUtils.VEC4_SIZE + VulkanUtils.VEC2_SIZE * 2 + VulkanUtils.INT_SIZE,
                    textureCache.getPosition(texturePath));

            // Padding
            data.putInt(offset + VulkanUtils.VEC4_SIZE + VulkanUtils.VEC2_SIZE * 2 + VulkanUtils.INT_SIZE * 2, 0);
            data.putInt(offset + VulkanUtils.VEC4_SIZE + VulkanUtils.VEC2_SIZE * 2 + VulkanUtils.INT_SIZE * 3, 0);

            offset += MATERIAL_SIZE;
        }
        srcBuffer.unmap(vkCtx);

        transferBuffer.recordTransferCommand(cmd);

        cmd.endRecording();
        cmd.submitAndWait(vkCtx, queue);
        cmd.cleanup(vkCtx, cmdPool);

        transferBuffer.srcBuffer().cleanup(vkCtx);
    }
}
