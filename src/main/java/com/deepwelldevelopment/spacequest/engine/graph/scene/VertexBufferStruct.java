package com.deepwelldevelopment.spacequest.engine.graph.scene;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;

import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanUtils;

public class VertexBufferStruct {

    private static final int NUMBER_OF_ATTRIBUTES = 2;
    private static final int POSITION_COMPONENTS = 3;
    private static final int TEX_COORD_COMPONENTS = 2;

    private final VkPipelineVertexInputStateCreateInfo vi;
    private final VkVertexInputAttributeDescription.Buffer viAttrBuffer;
    private final VkVertexInputBindingDescription.Buffer viBindings;

    public VertexBufferStruct() {
        viAttrBuffer = VkVertexInputAttributeDescription.calloc(NUMBER_OF_ATTRIBUTES);
        viBindings = VkVertexInputBindingDescription.calloc(1);
        vi = VkPipelineVertexInputStateCreateInfo.calloc();

        int i = 0;
        int offset = 0;
        // Position
        viAttrBuffer.get(i)
                .binding(0)
                .location(i)
                .format(VK_FORMAT_R32G32B32_SFLOAT)
                .offset(offset);

        // Texture coordinates
        i++;
        offset += POSITION_COMPONENTS * VulkanUtils.FLOAT_SIZE;
        viAttrBuffer.get(i)
                .binding(0)
                .location(i)
                .format(VK_FORMAT_R32G32_SFLOAT)
                .offset(offset);

        int stride = offset + TEX_COORD_COMPONENTS * VulkanUtils.FLOAT_SIZE;
        viBindings.get(0)
                .binding(0)
                .stride(stride)
                .inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

        vi
                .sType$Default()
                .pVertexBindingDescriptions(viBindings)
                .pVertexAttributeDescriptions(viAttrBuffer);
    }

    public void cleanup() {
        viBindings.free();
        viAttrBuffer.free();
    }

    public VkPipelineVertexInputStateCreateInfo getVi() {
        return vi;
    }
}
