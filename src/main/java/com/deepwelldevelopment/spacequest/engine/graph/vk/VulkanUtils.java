package com.deepwelldevelopment.spacequest.engine.graph.vk;

import static org.lwjgl.vulkan.VK10.VK_ERROR_DEVICE_LOST;
import static org.lwjgl.vulkan.VK10.VK_ERROR_EXTENSION_NOT_PRESENT;
import static org.lwjgl.vulkan.VK10.VK_ERROR_FEATURE_NOT_PRESENT;
import static org.lwjgl.vulkan.VK10.VK_ERROR_FORMAT_NOT_SUPPORTED;
import static org.lwjgl.vulkan.VK10.VK_ERROR_FRAGMENTED_POOL;
import static org.lwjgl.vulkan.VK10.VK_ERROR_INCOMPATIBLE_DRIVER;
import static org.lwjgl.vulkan.VK10.VK_ERROR_INITIALIZATION_FAILED;
import static org.lwjgl.vulkan.VK10.VK_ERROR_LAYER_NOT_PRESENT;
import static org.lwjgl.vulkan.VK10.VK_ERROR_MEMORY_MAP_FAILED;
import static org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY;
import static org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY;
import static org.lwjgl.vulkan.VK10.VK_ERROR_TOO_MANY_OBJECTS;
import static org.lwjgl.vulkan.VK10.VK_ERROR_UNKNOWN;
import static org.lwjgl.vulkan.VK10.VK_EVENT_RESET;
import static org.lwjgl.vulkan.VK10.VK_EVENT_SET;
import static org.lwjgl.vulkan.VK10.VK_INCOMPLETE;
import static org.lwjgl.vulkan.VK10.VK_NOT_READY;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_TIMEOUT;

import java.util.Locale;

public class VulkanUtils {

    public enum OSType {
        WINDOWS, MACOS, LINUX, OTHER
    }

    public static OSType getOS() {
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (os.indexOf("mac") >= 0 || (os.indexOf("darwin") >= 0)) {
            return OSType.MACOS;
        } else if (os.indexOf("win") >= 0) {
            return OSType.WINDOWS;
        } else if (os.indexOf("nux") >= 0) {
            return OSType.LINUX;
        } else {
            return OSType.OTHER;
        }
    }

    public static void vkCheck(int err, String errMsg) {
        if (err != VK_SUCCESS) {
            String errCode = switch (err) {
                case VK_NOT_READY -> "VK_NOT_READY";
                case VK_TIMEOUT -> "VK_TIMEOUT";
                case VK_EVENT_SET -> "VK_EVENT_SET";
                case VK_EVENT_RESET -> "VK_EVENT_RESET";
                case VK_INCOMPLETE -> "VK_INCOMPLETE";
                case VK_ERROR_OUT_OF_HOST_MEMORY -> "VK_ERROR_OUT_OF_HOST_MEMORY";
                case VK_ERROR_OUT_OF_DEVICE_MEMORY -> "VK_ERROR_OUT_OF_DEVICE_MEMORY";
                case VK_ERROR_INITIALIZATION_FAILED -> "VK_ERROR_INITIALIZATION_FAILED";
                case VK_ERROR_DEVICE_LOST -> "VK_ERROR_DEVICE_LOST";
                case VK_ERROR_MEMORY_MAP_FAILED -> "VK_ERROR_MEMORY_MAP_FAILED";
                case VK_ERROR_LAYER_NOT_PRESENT -> "VK_ERROR_LAYER_NOT_PRESENT";
                case VK_ERROR_EXTENSION_NOT_PRESENT -> "VK_ERROR_EXTENSION_NOT_PRESENT";
                case VK_ERROR_FEATURE_NOT_PRESENT -> "VK_ERROR_FEATURE_NOT_PRESENT";
                case VK_ERROR_INCOMPATIBLE_DRIVER -> "VK_ERROR_INCOMPATIBLE_DRIVER";
                case VK_ERROR_TOO_MANY_OBJECTS -> "VK_ERROR_TOO_MANY_OBJECTS";
                case VK_ERROR_FORMAT_NOT_SUPPORTED -> "VK_ERROR_FORMAT_NOT_SUPPORTED";
                case VK_ERROR_FRAGMENTED_POOL -> "VK_ERROR_FRAGMENTED_POOL";
                case VK_ERROR_UNKNOWN -> "VK_ERROR_UNKNOWN";
                default -> "Not mapped";
            };
            throw new RuntimeException(errMsg + ": " + errCode + " [" + err + "]");
        }
    }
}
