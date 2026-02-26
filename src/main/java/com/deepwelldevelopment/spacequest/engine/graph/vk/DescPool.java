package com.deepwelldevelopment.spacequest.engine.graph.vk;

import static com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanUtils.vkCheck;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorPool;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorPool;
import static org.lwjgl.vulkan.VK10.vkFreeDescriptorSets;

import java.nio.LongBuffer;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.tinylog.Logger;

public class DescPool {
    private final long vkDescPool;

    private List<DescTypeCount> descTypeCounts;

    public DescPool(Device device, List<DescTypeCount> descTypeCounts) {
        Logger.debug("Creating descriptor pool");
        this.descTypeCounts = descTypeCounts;
        try (var stack = MemoryStack.stackPush()) {
            int maxSets = 0;
            int numTypes = descTypeCounts.size();
            var typeCounts = VkDescriptorPoolSize.calloc(numTypes, stack);
            for (int i = 0; i < numTypes; i++) {
                maxSets += descTypeCounts.get(i).count();
                typeCounts.get(i)
                        .type(descTypeCounts.get(i).descType())
                        .descriptorCount(descTypeCounts.get(i).count());
            }

            var descriptorPoolInfo = VkDescriptorPoolCreateInfo.calloc(stack)
                    .sType$Default()
                    .flags(VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT)
                    .pPoolSizes(typeCounts)
                    .maxSets(maxSets);

            LongBuffer pDescriptorPool = stack.mallocLong(1);
            vkCheck(vkCreateDescriptorPool(device.getVkDevice(), descriptorPoolInfo, null, pDescriptorPool),
                    "Failed to create descriptor pool");
            vkDescPool = pDescriptorPool.get(0);
        }
    }

    public void cleanup(Device device) {
        Logger.debug("Destroying descriptor pool");
        vkDestroyDescriptorPool(device.getVkDevice(), vkDescPool, null);
    }

    public void freeDescriptorSet(Device device, long vkDescriptorSet) {
        try (var stack = MemoryStack.stackPush()) {
            LongBuffer longBuffer = stack.mallocLong(1);
            longBuffer.put(0, vkDescriptorSet);

            vkCheck(vkFreeDescriptorSets(device.getVkDevice(), vkDescPool, longBuffer),
                    "Failed to free descriptor set");
        }
    }

    public List<DescTypeCount> getDescTypeCounts() {
        return descTypeCounts;
    }

    public long getVkDescPool() {
        return vkDescPool;
    }

    public record DescTypeCount(int descType, int count) {
    }
}
