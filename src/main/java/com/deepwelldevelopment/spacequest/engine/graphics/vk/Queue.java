package com.deepwelldevelopment.spacequest.engine.graphics.vk;

import static com.deepwelldevelopment.spacequest.engine.graphics.vk.VulkanUtils.vkCheck;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK10.VK_TRUE;
import static org.lwjgl.vulkan.VK10.vkGetDeviceQueue;
import static org.lwjgl.vulkan.VK10.vkQueueWaitIdle;
import static org.lwjgl.vulkan.VK13.vkQueueSubmit2;

import java.nio.IntBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VkCommandBufferSubmitInfo;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkSemaphoreSubmitInfo;
import org.lwjgl.vulkan.VkSubmitInfo2;
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

    public void submit(VkCommandBufferSubmitInfo.Buffer commandBuffers, VkSemaphoreSubmitInfo.Buffer waitSemaphores,
            VkSemaphoreSubmitInfo.Buffer signalSemaphores, Fence fence) {
        try (var stack = MemoryStack.stackPush()) {
            var submitInfo = VkSubmitInfo2.calloc(1, stack).sType$Default().pCommandBufferInfos(commandBuffers)
                    .pSignalSemaphoreInfos(signalSemaphores);
            if (waitSemaphores != null) {
                submitInfo.pWaitSemaphoreInfos(waitSemaphores);
            }
            long fenceHandle = fence != null ? fence.getVkFence() : VK_NULL_HANDLE;
            vkCheck(vkQueueSubmit2(vkQueue, submitInfo, fenceHandle), "Failed to submit command buffer");
        }
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

    public static class PresentQueue extends Queue {

        public PresentQueue(VulkanContext vkCtx, int queueIndex) {
            super(vkCtx, getPresentQueueFamilyIndex(vkCtx), queueIndex);
        }

        private static int getPresentQueueFamilyIndex(VulkanContext vkCtx) {
            int index = -1;
            try (var stack = MemoryStack.stackPush()) {
                var queuePropsBuff = vkCtx.getPhysicalDevice().getVkQueueFamilyProps();
                int numQueuesFamilies = queuePropsBuff.capacity();
                IntBuffer intBuff = stack.mallocInt(1);
                for (int i = 0; i < numQueuesFamilies; i++) {
                    KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(vkCtx.getPhysicalDevice().getVkPhysicalDevice(), i,
                            vkCtx.getSurface().getVkSurface(), intBuff);
                    boolean supportsPresentation = intBuff.get(0) == VK_TRUE;
                    if (supportsPresentation) {
                        index = i;
                        break;
                    }
                }
            }

            if (index < 0) {
                throw new RuntimeException("Failed to get Presentation Queue family index");
            }
            return index;
        }
    }
}
