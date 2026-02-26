package com.deepwelldevelopment.spacequest.engine.graph.vk;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_UNDEFINED;

import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;

public class PipelineBuildInfo {

    private final int colorFormat;
    private final ShaderModule[] shaderModules;
    private final VkPipelineVertexInputStateCreateInfo vi;
    private int depthFormat;
    private PushConstRange[] pushConstantRanges;

    public PipelineBuildInfo(ShaderModule[] shaderModules, VkPipelineVertexInputStateCreateInfo vi, int colorFormat) {
        this.shaderModules = shaderModules;
        this.vi = vi;
        this.colorFormat = colorFormat;
        depthFormat = VK_FORMAT_UNDEFINED;
    }

    public int getColorFormat() {
        return colorFormat;
    }

    public ShaderModule[] getShaderModules() {
        return shaderModules;
    }

    public VkPipelineVertexInputStateCreateInfo getVi() {
        return vi;
    }

    public int getDepthFormat() {
        return depthFormat;
    }

    public PushConstRange[] getPushConstRanges() {
        return pushConstantRanges;
    }

    public PipelineBuildInfo setDepthFormat(int depthFormat) {
        this.depthFormat = depthFormat;
        return this;
    }

    public PipelineBuildInfo setPushConstRanges(PushConstRange[] pushConstantRanges) {
        this.pushConstantRanges = pushConstantRanges;
        return this;
    }
}
