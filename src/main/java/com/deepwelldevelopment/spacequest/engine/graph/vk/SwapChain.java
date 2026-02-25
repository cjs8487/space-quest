package com.deepwelldevelopment.spacequest.engine.graph.vk;

import static com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanUtils.vkCheck;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.tinylog.Logger;

import com.deepwelldevelopment.spacequest.engine.window.Window;

public class SwapChain {

    private final ImageView[] imageViews;
    private final int numImages;
    private final VkExtent2D swapChainExtent;
    private final long vkSwapChain;

    public SwapChain(Window window, Device device, Surface surface, int requestedImages, boolean vsync) {
        Logger.debug("Creating Vulkan SwapChain");
        try (var stack = MemoryStack.stackPush()) {
            VkSurfaceCapabilitiesKHR surfaceCaps = surface.getSurfaceCaps();

            int reqImages = calcNumImages(surfaceCaps, requestedImages);
            swapChainExtent = calcSwapChainExtent(surfaceCaps, window);

            Surface.SurfaceFormat surfaceFormat = surface.getSurfaceFormat();
            var vkSwapchainCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack)
                    .sType$Default()
                    .surface(surface.getVkSurface())
                    .minImageCount(reqImages)
                    .imageFormat(surfaceFormat.format())
                    .imageColorSpace(surfaceFormat.colorSpace())
                    .imageExtent(swapChainExtent)
                    .imageArrayLayers(1)
                    .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                    .preTransform(surfaceCaps.currentTransform())
                    .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                    .clipped(true);
            if (vsync) {
                vkSwapchainCreateInfo.presentMode(KHRSurface.VK_PRESENT_MODE_FIFO_KHR);
            } else {
                vkSwapchainCreateInfo.presentMode(KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR);
            }

            LongBuffer lp = stack.mallocLong(1);
            vkCheck(KHRSwapchain.vkCreateSwapchainKHR(device.getVkDevice(), vkSwapchainCreateInfo, null, lp),
                    "Failed to create swap chain");
            vkSwapChain = lp.get(0);

            imageViews = createImageViews(stack, device, vkSwapChain, surfaceFormat.format());
            numImages = imageViews.length;
        }
    }

    private int calcNumImages(VkSurfaceCapabilitiesKHR surfaceCaps, int requestedImages) {
        int maxImages = surfaceCaps.maxImageCount();
        int minImages = surfaceCaps.minImageCount();
        int result = minImages;
        if (maxImages != 0) {
            result = Math.min(requestedImages, maxImages);
        }
        result = Math.max(result, minImages);
        Logger.debug("Requested [{}] images, got [{}]. Surface capabilities, maxImages: [{}], minImages: [{}]",
                requestedImages, result, maxImages, minImages);
        return result;
    }

    private VkExtent2D calcSwapChainExtent(VkSurfaceCapabilitiesKHR surfaceCaps, Window window) {
        var result = VkExtent2D.calloc();
        if (surfaceCaps.currentExtent().width() == 0xFFFFFFFF) {
            int width = Math.min(window.getWidth(), surfaceCaps.maxImageExtent().width());
            width = Math.max(width, surfaceCaps.minImageExtent().width());

            int height = Math.min(window.getHeight(), surfaceCaps.maxImageExtent().height());
            height = Math.max(height, surfaceCaps.minImageExtent().height());

            result.width(width);
            result.height(height);
        } else {
            result.set(surfaceCaps.currentExtent());
        }
        return result;
    }

    private static ImageView[] createImageViews(MemoryStack stack, Device device, long swapChain, int format) {
        IntBuffer ip = stack.mallocInt(1);
        vkCheck(KHRSwapchain.vkGetSwapchainImagesKHR(device.getVkDevice(), swapChain, ip, null),
                "Failed to get number of surface images");
        int numImages = ip.get(0);

        LongBuffer swapChainImages = stack.mallocLong(numImages);
        vkCheck(KHRSwapchain.vkGetSwapchainImagesKHR(device.getVkDevice(), swapChain, ip, swapChainImages),
                "Failed to get surface images");

        var result = new ImageView[numImages];
        var imageViewData = new ImageView.ImageViewData().format(format).aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
        for (int i = 0; i < numImages; i++) {
            result[i] = new ImageView(device, swapChainImages.get(i), imageViewData);
        }

        return result;
    }

    public void cleanup(Device device) {
        Logger.debug("Destroying Vulkan SwapChain");
        swapChainExtent.free();
        Arrays.asList(imageViews).forEach(i -> i.cleanup(device));
        KHRSwapchain.vkDestroySwapchainKHR(device.getVkDevice(), vkSwapChain, null);
    }

    public int acquireNextImage(Device device, Semaphore semaphore) {
        int imageIndex;
        try (var stack = MemoryStack.stackPush()) {
            IntBuffer ip = stack.mallocInt(1);
            int err = KHRSwapchain.vkAcquireNextImageKHR(device.getVkDevice(), vkSwapChain, ~0L,
                    semaphore.getVkSemaphore(), MemoryUtil.NULL, ip);
            if (err == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
                return -1;
            } else if (err == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                // Not optimal but swapchain can still be used
            } else if (err != VK_SUCCESS) {
                throw new RuntimeException("Failed to acquire next image: " + err);
            }
            imageIndex = ip.get(0);
        }
        return imageIndex;
    }

    public boolean presentImage(Queue queue, Semaphore renderCompleteSemaphore, int imageIndex) {
        boolean resize = false;
        try (var stack = MemoryStack.stackPush()) {
            var presentInfo = VkPresentInfoKHR.calloc(stack)
                    .sType$Default()
                    .pWaitSemaphores(stack.longs(renderCompleteSemaphore.getVkSemaphore()))
                    .swapchainCount(1)
                    .pSwapchains(stack.longs(vkSwapChain))
                    .pImageIndices(stack.ints(imageIndex));

            int err = KHRSwapchain.vkQueuePresentKHR(queue.getVkQueue(), presentInfo);
            if (err == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || err == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                resize = true;
            } else if (err == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                // Not optimal but swapchain can still be used
            } else if (err != VK_SUCCESS) {
                throw new RuntimeException("Failed to present image: " + err);
            }
        }
        return resize;
    }

    public ImageView getImageView(int index) {
        return imageViews[index];
    }

    public int getNumImages() {
        return numImages;
    }

    public VkExtent2D getSwapChainExtent() {
        return swapChainExtent;
    }
}
