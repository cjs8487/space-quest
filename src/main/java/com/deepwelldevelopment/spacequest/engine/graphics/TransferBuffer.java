package com.deepwelldevelopment.spacequest.engine.graphics;

import static org.lwjgl.vulkan.VK10.vkCmdCopyBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCopy;

import com.deepwelldevelopment.spacequest.engine.graphics.vk.CommandBuffer;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.VulkanBuffer;

public record TransferBuffer(VulkanBuffer srcBuffer, VulkanBuffer dstBuffer) {

    public void recordTransferCommand(CommandBuffer cmd) {
        try (var stack = MemoryStack.stackPush()) {
            VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc(1, stack).srcOffset(0).dstOffset(0)
                    .size(srcBuffer.getRequestedSize());
            vkCmdCopyBuffer(cmd.getVkCommandBuffer(), srcBuffer.getBuffer(), dstBuffer.getBuffer(), copyRegion);
        }
    }
}