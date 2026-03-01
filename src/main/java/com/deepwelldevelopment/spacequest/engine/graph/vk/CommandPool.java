package com.deepwelldevelopment.spacequest.engine.graph.vk;

import static com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanUtils.vkCheck;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.vkCreateCommandPool;
import static org.lwjgl.vulkan.VK10.vkDestroyCommandPool;
import static org.lwjgl.vulkan.VK10.vkResetCommandPool;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.tinylog.Logger;

public class CommandPool {

    private final long vkCommandPool;

    public CommandPool(VulkanContext context, int queueFamilyIndex, boolean supportReset) {
        Logger.debug("Creating Vulkan command pool");

        try (var stack = MemoryStack.stackPush()) {
            var cmdPoolInfo = VkCommandPoolCreateInfo.calloc(stack)
                    .sType$Default()
                    .queueFamilyIndex(queueFamilyIndex);
            if (supportReset) {
                cmdPoolInfo.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
            }

            LongBuffer lp = stack.mallocLong(1);
            vkCheck(vkCreateCommandPool(context.getDevice().getVkDevice(), cmdPoolInfo, null, lp),
                    "Failed to create command pool");

            this.vkCommandPool = lp.get(0);
        }
    }

    public void cleanup(VulkanContext context) {
        Logger.debug("Destroying Vulkan command pool");
        vkDestroyCommandPool(context.getDevice().getVkDevice(), vkCommandPool, null);
    }

    public long getVkCommandPool() {
        return vkCommandPool;
    }

    public void reset(VulkanContext context) {
        vkResetCommandPool(context.getDevice().getVkDevice(), vkCommandPool, 0);
    }
}
