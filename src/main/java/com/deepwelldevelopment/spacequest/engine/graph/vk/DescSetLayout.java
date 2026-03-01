package com.deepwelldevelopment.spacequest.engine.graph.vk;

import static com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanUtils.vkCheck;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorSetLayout;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.tinylog.Logger;

public class DescSetLayout {

    private final LayoutInfo[] layoutInfos;
    protected long vkDescLayout;

    public DescSetLayout(VulkanContext vkCtx, LayoutInfo layoutInfo) {
        this(vkCtx, new DescSetLayout.LayoutInfo[] { layoutInfo });
    }

    public DescSetLayout(VulkanContext vkCtx, LayoutInfo[] layoutInfos) {
        this.layoutInfos = layoutInfos;
        try (var stack = MemoryStack.stackPush()) {
            int count = layoutInfos.length;
            var layoutBindings = VkDescriptorSetLayoutBinding.calloc(count, stack);
            for (int i = 0; i < count; i++) {
                LayoutInfo layoutInfo = layoutInfos[i];
                layoutBindings.get(i)
                        .binding(layoutInfo.binding())
                        .descriptorType(layoutInfo.descType())
                        .descriptorCount(layoutInfo.descCount())
                        .stageFlags(layoutInfo.stage());
            }

            var vkLayoutInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(layoutBindings);

            LongBuffer pSetLayout = stack.mallocLong(1);
            vkCheck(vkCreateDescriptorSetLayout(vkCtx.getDevice().getVkDevice(), vkLayoutInfo, null, pSetLayout),
                    "Failed to create descriptor set layout");
            vkDescLayout = pSetLayout.get(0);
        }
    }

    public void cleanup(VulkanContext vkCtx) {
        Logger.debug("Destroying descriptor set layout");
        vkDestroyDescriptorSetLayout(vkCtx.getDevice().getVkDevice(), vkDescLayout, null);
    }

    public LayoutInfo getLayoutInfo() {
        return getLayoutInfos()[0];
    }

    public LayoutInfo[] getLayoutInfos() {
        return layoutInfos;
    }

    public long getVkDescLayout() {
        return vkDescLayout;
    }

    public record LayoutInfo(int descType, int binding, int descCount, int stage) {
    }
}
