package com.deepwelldevelopment.spacequest.engine.graph.vk;

import static org.lwjgl.vulkan.VK10.vkCreateSemaphore;
import static org.lwjgl.vulkan.VK10.vkDestroySemaphore;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

public class Semaphore {

    private final long vkSemaphore;

    public Semaphore(VulkanContext context) {
        try (var stack = MemoryStack.stackPush()) {
            var semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc(stack)
                    .sType$Default();
            LongBuffer lp = stack.mallocLong(1);
            VulkanUtils.vkCheck(vkCreateSemaphore(context.getDevice().getVkDevice(), semaphoreCreateInfo, null, lp),
                    "Failed to create semaphore");
            this.vkSemaphore = lp.get(0);
        }
    }

    public void cleanup(VulkanContext context) {
        vkDestroySemaphore(context.getDevice().getVkDevice(), vkSemaphore, null);
    }

    public long getVkSemaphore() {
        return vkSemaphore;
    }
}
