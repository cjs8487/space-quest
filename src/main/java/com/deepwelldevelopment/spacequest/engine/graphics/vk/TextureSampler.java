package com.deepwelldevelopment.spacequest.engine.graphics.vk;

import static com.deepwelldevelopment.spacequest.engine.graphics.vk.VulkanUtils.vkCheck;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_NEVER;
import static org.lwjgl.vulkan.VK10.VK_FILTER_NEAREST;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST;
import static org.lwjgl.vulkan.VK10.vkCreateSampler;
import static org.lwjgl.vulkan.VK10.vkDestroySampler;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

public class TextureSampler {

    private static final int MAX_ANISOTROPY = 16;

    private final long vkSampler;

    public TextureSampler(VulkanContext vkCtx, TextureSamplerInfo textureSamplerInfo) {
        try (var stack = MemoryStack.stackPush()) {
            var samplerInfo = VkSamplerCreateInfo.calloc(stack).sType$Default().magFilter(VK_FILTER_NEAREST)
                    .minFilter(VK_FILTER_NEAREST).addressModeU(textureSamplerInfo.addressMode())
                    .addressModeV(textureSamplerInfo.addressMode()).addressModeW(textureSamplerInfo.addressMode())
                    .borderColor(textureSamplerInfo.borderColor()).unnormalizedCoordinates(false).compareEnable(false)
                    .compareOp(VK_COMPARE_OP_NEVER).mipmapMode(VK_SAMPLER_MIPMAP_MODE_NEAREST).minLod(0.0f)
                    .maxLod(textureSamplerInfo.mipLevels()).mipLodBias(0.0f);
            if (textureSamplerInfo.anisotropy() && vkCtx.getDevice().isSamplerAnisotropy()) {
                samplerInfo.anisotropyEnable(true).maxAnisotropy(MAX_ANISOTROPY);
            }

            LongBuffer lp = stack.mallocLong(1);
            vkCheck(vkCreateSampler(vkCtx.getDevice().getVkDevice(), samplerInfo, null, lp),
                    "Failed to create sampler");
            vkSampler = lp.get(0);
        }
    }

    public void cleanup(VulkanContext vkCtx) {
        vkDestroySampler(vkCtx.getDevice().getVkDevice(), vkSampler, null);
    }

    public long getVkSampler() {
        return vkSampler;
    }
}
