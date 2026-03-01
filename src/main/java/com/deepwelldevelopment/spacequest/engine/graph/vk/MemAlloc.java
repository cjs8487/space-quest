package com.deepwelldevelopment.spacequest.engine.graph.vk;

import static com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanUtils.vkCheck;
import static org.lwjgl.util.vma.Vma.vmaCreateAllocator;
import static org.lwjgl.util.vma.Vma.vmaDestroyAllocator;
import static org.lwjgl.vulkan.VK13.VK_API_VERSION_1_3;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;

public class MemAlloc {

    private final long vmaAlloc;

    public MemAlloc(Instance instance, PhysicalDevice physDevice, Device device) {
        try (var stack = MemoryStack.stackPush()) {
            PointerBuffer pAllocator = stack.mallocPointer(1);

            var vmaVulkanFunctions = VmaVulkanFunctions.calloc(stack).set(instance.getVkInstance(),
                    device.getVkDevice());

            var createInfo = VmaAllocatorCreateInfo.calloc(stack).instance(instance.getVkInstance())
                    .vulkanApiVersion(VK_API_VERSION_1_3).device(device.getVkDevice())
                    .physicalDevice(physDevice.getVkPhysicalDevice()).pVulkanFunctions(vmaVulkanFunctions);
            vkCheck(vmaCreateAllocator(createInfo, pAllocator), "Failed to create VMA allocator");

            vmaAlloc = pAllocator.get(0);
        }
    }

    public void cleanup() {
        vmaDestroyAllocator(vmaAlloc);
    }

    public long getVmaAlloc() {
        return vmaAlloc;
    }
}
