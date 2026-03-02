package com.deepwelldevelopment.spacequest.engine.graphics.vk;

import static com.deepwelldevelopment.spacequest.engine.graphics.vk.VulkanUtils.vkCheck;
import static org.lwjgl.vulkan.VK10.VK_FENCE_CREATE_SIGNALED_BIT;
import static org.lwjgl.vulkan.VK10.vkCreateFence;
import static org.lwjgl.vulkan.VK10.vkDestroyFence;
import static org.lwjgl.vulkan.VK10.vkResetFences;
import static org.lwjgl.vulkan.VK10.vkWaitForFences;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkFenceCreateInfo;

public class Fence {

    private final long vkFence;

    public Fence(VulkanContext vkCtx, boolean signaled) {
        try (var stack = MemoryStack.stackPush()) {
            var fenceCreateInfo = VkFenceCreateInfo.calloc(stack).sType$Default()
                    .flags(signaled ? VK_FENCE_CREATE_SIGNALED_BIT : 0);

            LongBuffer lp = stack.mallocLong(1);
            vkCheck(vkCreateFence(vkCtx.getDevice().getVkDevice(), fenceCreateInfo, null, lp),
                    "Failed to create fence");
            vkFence = lp.get(0);
        }
    }

    public void cleanup(VulkanContext vkCtx) {
        vkDestroyFence(vkCtx.getDevice().getVkDevice(), vkFence, null);
    }

    public void fenceWait(VulkanContext vkCtx) {
        vkWaitForFences(vkCtx.getDevice().getVkDevice(), vkFence, true, Long.MAX_VALUE);
    }

    public long getVkFence() {
        return vkFence;
    }

    public void reset(VulkanContext vkCtx) {
        vkResetFences(vkCtx.getDevice().getVkDevice(), vkFence);
    }
}
