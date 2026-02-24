package com.deepwelldevelopment.spacequest.engine.graph.vk;

import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK10.vkGetDeviceQueue;
import static org.lwjgl.vulkan.VK10.vkQueueWaitIdle;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.tinylog.Logger;

public class Queue {

    private final int queueFamilyIndex;
    private final VkQueue vkQueue;

    public Queue(VulkanContext vulkanContext, int queueFamilyIndex, int queueIndex) {
        Logger.debug("Creating queue");

        this.queueFamilyIndex = queueFamilyIndex;
        try (var stack = MemoryStack.stackPush()) {
            PointerBuffer pQueue = stack.mallocPointer(1);
            vkGetDeviceQueue(vulkanContext.getDevice().getVkDevice(), queueFamilyIndex, queueIndex, pQueue);
            long queue = pQueue.get(0);
            this.vkQueue = new VkQueue(queue, vulkanContext.getDevice().getVkDevice());
        }
    }

    public int getQueueFamilyIndex() {
        return queueFamilyIndex;
    }

    public VkQueue getVkQueue() {
        return vkQueue;
    }

    public void waitIdle() {
        vkQueueWaitIdle(vkQueue);
    }

    public static class GraphicsQueue extends Queue {
        public GraphicsQueue(VulkanContext vulkanContext, int queueIndex) {
            super(vulkanContext, getGraphicsQueueFamilyIndex(vulkanContext), queueIndex);
        }

        public static int getGraphicsQueueFamilyIndex(VulkanContext vulkanContext) {
            int index = -1;
            var queuePropsBuff = vulkanContext.getPhysicalDevice().getVkQueueFamilyProps();
            for (int i = 0; i < queuePropsBuff.capacity(); i++) {
                VkQueueFamilyProperties props = queuePropsBuff.get(i);
                boolean graphicsQueue = (props.queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0;
                if (graphicsQueue) {
                    index = i;
                    break;
                }
            }

            if (index < 0) {
                throw new RuntimeException("Failed to get graphics queue family index");
            }
            return index;
        }
    }
}
