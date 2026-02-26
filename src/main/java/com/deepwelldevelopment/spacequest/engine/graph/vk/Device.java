package com.deepwelldevelopment.spacequest.engine.graph.vk;

import static com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanUtils.vkCheck;
import static org.lwjgl.vulkan.KHRPortabilitySubset.VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.vkCreateDevice;
import static org.lwjgl.vulkan.VK10.vkDestroyDevice;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;
import static org.lwjgl.vulkan.VK10.vkEnumerateDeviceExtensionProperties;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan13Features;
import org.tinylog.Logger;

public class Device {

    private final VkDevice vkDevice;
    private final boolean samplerAnisotropy;

    public Device(PhysicalDevice physicalDevice) {
        Logger.debug("Creating logical device");
        try (var stack = MemoryStack.stackPush()) {
            PointerBuffer reqExtensions = createReqExtensions(physicalDevice, stack);

            // Enable all the queue families
            var queuePropsBuff = physicalDevice.getVkQueueFamilyProps();
            int numQueueFamilies = queuePropsBuff.capacity();
            var queueCreationInfoBuf = VkDeviceQueueCreateInfo.calloc(numQueueFamilies, stack);
            for (int i = 0; i < numQueueFamilies; i++) {
                FloatBuffer priorities = stack.callocFloat(queuePropsBuff.get(i).queueCount());
                queueCreationInfoBuf.get(i)
                        .sType$Default()
                        .queueFamilyIndex(i)
                        .pQueuePriorities(priorities);
            }

            // Set up required features
            var features13 = VkPhysicalDeviceVulkan13Features.calloc(stack)
                    .sType$Default()
                    .dynamicRendering(true)
                    .synchronization2(true);

            var features2 = VkPhysicalDeviceFeatures2.calloc(stack).sType$Default();
            var features = features2.features();

            VkPhysicalDeviceFeatures supportedFeatures = physicalDevice.getVkPhysicalDeviceFeatures();
            samplerAnisotropy = supportedFeatures.samplerAnisotropy();
            if (samplerAnisotropy) {
                features.samplerAnisotropy(true);
            }
            features2.pNext(features13.address());

            var deviceCreateInfo = VkDeviceCreateInfo.calloc(stack)
                    .sType$Default()
                    .pNext(features2.address())
                    .ppEnabledExtensionNames(reqExtensions)
                    .pQueueCreateInfos(queueCreationInfoBuf);

            PointerBuffer pp = stack.mallocPointer(1);
            vkCheck(vkCreateDevice(physicalDevice.getVkPhysicalDevice(), deviceCreateInfo, null, pp),
                    "Failed to create device");
            this.vkDevice = new VkDevice(pp.get(0), physicalDevice.getVkPhysicalDevice(), deviceCreateInfo);
        }
    }

    private static PointerBuffer createReqExtensions(PhysicalDevice physicalDevice, MemoryStack stack) {
        Set<String> deviceExtensions = getDeviceExtensions(physicalDevice);
        boolean usePortability = deviceExtensions.contains(VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME);

        var extsList = new ArrayList<ByteBuffer>();
        for (String extension : PhysicalDevice.REQUIRED_EXTENSIONS) {
            extsList.add(stack.ASCII(extension));
        }
        if (usePortability) {
            extsList.add(stack.ASCII(VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME));
        }

        PointerBuffer requiredExtensions = stack.mallocPointer(extsList.size());
        extsList.forEach(requiredExtensions::put);
        requiredExtensions.flip();

        return requiredExtensions;
    }

    private static Set<String> getDeviceExtensions(PhysicalDevice physicalDevice) {
        Set<String> deviceExtensions = new HashSet<>();
        try (var stack = MemoryStack.stackPush()) {
            IntBuffer numExtensionsBuf = stack.callocInt(1);
            vkEnumerateDeviceExtensionProperties(physicalDevice.getVkPhysicalDevice(), (String) null, numExtensionsBuf,
                    null);
            int numExtensions = numExtensionsBuf.get(0);
            Logger.trace("Device supports [{}] extensions", numExtensions);

            try (var propsBuff = VkExtensionProperties.calloc(numExtensions)) {
                vkEnumerateDeviceExtensionProperties(physicalDevice.getVkPhysicalDevice(), (String) null,
                        numExtensionsBuf,
                        propsBuff);
                for (int i = 0; i < numExtensions; i++) {
                    VkExtensionProperties props = propsBuff.get(i);
                    String extensionName = props.extensionNameString();
                    deviceExtensions.add(extensionName);
                    Logger.trace("Supported device extension: [{}]", extensionName);
                }
            }
        }
        return deviceExtensions;
    }

    public void cleanup() {
        Logger.debug("Destroying Vulkan device");
        vkDestroyDevice(vkDevice, null);
    }

    public VkDevice getVkDevice() {
        return vkDevice;
    }

    public void waitIdle() {
        vkDeviceWaitIdle(vkDevice);
    }

    public boolean isSamplerAnisotropy() {
        return samplerAnisotropy;
    }
}
