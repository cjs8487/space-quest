package com.deepwelldevelopment.spacequest.engine.graph.vk;

import static com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanUtils.vkCheck;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_B8G8R8A8_UNORM;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.tinylog.Logger;

import com.deepwelldevelopment.spacequest.engine.window.Window;

public class Surface {

    private final VkSurfaceCapabilitiesKHR surfaceCaps;
    private final SurfaceFormat surfaceFormat;
    private final long vkSurface;

    public Surface(Instance instance, PhysicalDevice physicalDevice, Window window) {
        Logger.debug("Creating Vulkan surface");
        try (var stack = MemoryStack.stackPush()) {
            LongBuffer pSurface = stack.mallocLong(1);
            GLFWVulkan.glfwCreateWindowSurface(instance.getVkInstance(), window.getHandle(), null, pSurface);
            this.vkSurface = pSurface.get(0);

            surfaceCaps = VkSurfaceCapabilitiesKHR.calloc();
            vkCheck(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice.getVkPhysicalDevice(),
                    vkSurface, surfaceCaps), "Failed to get surface capabilities");

            surfaceFormat = calcSurfaceFormat(physicalDevice, vkSurface);
        }
    }

    private static SurfaceFormat calcSurfaceFormat(PhysicalDevice physicalDevice, long vkSurface) {
        int imageFormat;
        int colorSpace;
        try (var stack = MemoryStack.stackPush()) {
            IntBuffer ip = stack.mallocInt(1);
            vkCheck(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice.getVkPhysicalDevice(), vkSurface, ip,
                    null), "Failed to get the number of surface formats");
            int numFormats = ip.get(0);
            if (numFormats <= 0) {
                throw new RuntimeException("No surface formats retrieved");
            }

            var surfaceFormats = VkSurfaceFormatKHR.calloc(numFormats, stack);
            vkCheck(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice.getVkPhysicalDevice(), vkSurface, ip,
                    surfaceFormats), "Failed to get surface formats");

            imageFormat = VK_FORMAT_B8G8R8A8_UNORM;
            colorSpace = surfaceFormats.get(0).colorSpace();
            for (int i = 0; i < numFormats; i++) {
                VkSurfaceFormatKHR surfaceFormatKHR = surfaceFormats.get(i);
                if (surfaceFormatKHR.format() == VK_FORMAT_B8G8R8A8_UNORM
                        && surfaceFormatKHR.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                    imageFormat = surfaceFormatKHR.format();
                    colorSpace = surfaceFormatKHR.colorSpace();
                    break;
                }
            }
            return new SurfaceFormat(imageFormat, colorSpace);
        }
    }

    public void cleanup(Instance instance) {
        Logger.debug("Destroying Vulkan surface");
        surfaceCaps.free();
        KHRSurface.vkDestroySurfaceKHR(instance.getVkInstance(), vkSurface, null);
    }

    public VkSurfaceCapabilitiesKHR getSurfaceCaps() {
        return surfaceCaps;
    }

    public SurfaceFormat getSurfaceFormat() {
        return surfaceFormat;
    }

    public long getVkSurface() {
        return vkSurface;
    }

    public record SurfaceFormat(int format, int colorSpace) {
    }
}
