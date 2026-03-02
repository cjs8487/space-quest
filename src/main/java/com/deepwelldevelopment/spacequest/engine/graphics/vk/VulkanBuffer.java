package com.deepwelldevelopment.spacequest.engine.graphics.vk;

import static com.deepwelldevelopment.spacequest.engine.graphics.vk.VulkanUtils.vkCheck;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.vma.Vma.vmaCreateBuffer;
import static org.lwjgl.util.vma.Vma.vmaDestroyBuffer;
import static org.lwjgl.util.vma.Vma.vmaFlushAllocation;
import static org.lwjgl.util.vma.Vma.vmaMapMemory;
import static org.lwjgl.util.vma.Vma.vmaUnmapMemory;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_WHOLE_SIZE;

import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;

public class VulkanBuffer {

    private final long allocation;
    private final long buffer;
    private final PointerBuffer pb;
    private final long requestedSize;

    private long mappedMemory;

    public VulkanBuffer(VulkanContext context, long size, int bufferUsage, int vmaUsage, int vmaFlags, int reqFlags) {
        requestedSize = size;
        mappedMemory = NULL;
        try (var stack = MemoryStack.stackPush()) {
            var bufferCreateInfo = VkBufferCreateInfo.calloc(stack).sType$Default().size(size).usage(bufferUsage)
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            VmaAllocationCreateInfo allocInfo = VmaAllocationCreateInfo.calloc(stack).usage(vmaUsage).flags(vmaFlags)
                    .requiredFlags(reqFlags);

            PointerBuffer pAllocation = stack.callocPointer(1);
            LongBuffer lp = stack.mallocLong(1);
            vkCheck(vmaCreateBuffer(context.getMemAlloc().getVmaAlloc(), bufferCreateInfo, allocInfo, lp, pAllocation,
                    null), "Failed to create buffer");
            buffer = lp.get(0);
            allocation = pAllocation.get(0);
            pb = MemoryUtil.memAllocPointer(1);
        }
    }

    public void cleanup(VulkanContext vkCtx) {
        MemoryUtil.memFree(pb);
        unmap(vkCtx);
        vmaDestroyBuffer(vkCtx.getMemAlloc().getVmaAlloc(), buffer, allocation);
    }

    public void flush(VulkanContext vkCtx) {
        vmaFlushAllocation(vkCtx.getMemAlloc().getVmaAlloc(), allocation, 0, VK_WHOLE_SIZE);
    }

    public long getBuffer() {
        return buffer;
    }

    public long getRequestedSize() {
        return requestedSize;
    }

    public long map(VulkanContext context) {
        if (mappedMemory == NULL) {
            vkCheck(vmaMapMemory(context.getMemAlloc().getVmaAlloc(), allocation, pb), "Failed to map buffer");
            mappedMemory = pb.get(0);
        }
        return mappedMemory;
    }

    public void unmap(VulkanContext context) {
        if (mappedMemory != NULL) {
            vmaUnmapMemory(context.getMemAlloc().getVmaAlloc(), allocation);
            mappedMemory = NULL;
        }
    }
}
