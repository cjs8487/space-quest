package com.deepwelldevelopment.spacequest.engine.graph.vk;

import static com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanUtils.vkCheck;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindBufferMemory;
import static org.lwjgl.vulkan.VK10.vkCreateBuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkGetBufferMemoryRequirements;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;

public class VulkanBuffer {

    private final long allocationSize;
    private final long buffer;
    private final long memory;
    private final PointerBuffer pb;
    private final long requestedSize;

    private long mappedMemory;

    public VulkanBuffer(VulkanContext context, long size, int usage, int reqMask) {
        requestedSize = size;
        mappedMemory = MemoryUtil.NULL;
        try (var stack = MemoryStack.stackPush()) {
            Device device = context.getDevice();
            var bufferCreateInfo = VkBufferCreateInfo.calloc(stack)
                    .sType$Default()
                    .size(size)
                    .usage(usage)
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE);
            LongBuffer lp = stack.mallocLong(1);
            vkCheck(vkCreateBuffer(device.getVkDevice(), bufferCreateInfo, null, lp), null);
            buffer = lp.get(0);

            var memoryRequirements = VkMemoryRequirements.calloc(stack);
            vkGetBufferMemoryRequirements(device.getVkDevice(), buffer, memoryRequirements);

            var memAllocInfo = VkMemoryAllocateInfo.calloc(stack)
                    .sType$Default()
                    .allocationSize(memoryRequirements.size())
                    .memoryTypeIndex(VulkanUtils.memoryTypeFromProperties(context, memoryRequirements.memoryTypeBits(),
                            reqMask));

            vkCheck(vkAllocateMemory(device.getVkDevice(), memAllocInfo, null, lp), "Failed to allocate memory");
            allocationSize = memAllocInfo.allocationSize();
            memory = lp.get(0);
            pb = MemoryUtil.memAllocPointer(1);

            vkCheck(vkBindBufferMemory(device.getVkDevice(), buffer, memory, 0), "Failed to bind buffer memory");
        }
    }

    public void cleanup(VulkanContext context) {
        MemoryUtil.memFree(pb);
        VkDevice vkDevice = context.getDevice().getVkDevice();
        vkDestroyBuffer(vkDevice, buffer, null);
        vkFreeMemory(vkDevice, memory, null);
    }

    public long getBuffer() {
        return buffer;
    }

    public long getRequestedSize() {
        return requestedSize;
    }

    public long map(VulkanContext context) {
        if (mappedMemory == NULL) {
            vkCheck(vkMapMemory(context.getDevice().getVkDevice(), memory, 0, allocationSize, 0, pb),
                    "Failed to map Buffer");
            mappedMemory = pb.get(0);
        }
        return mappedMemory;
    }

    public void unmap(VulkanContext context) {
        if (mappedMemory != NULL) {
            vkUnmapMemory(context.getDevice().getVkDevice(), memory);
            mappedMemory = NULL;
        }
    }
}
