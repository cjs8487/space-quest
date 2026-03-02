package com.deepwelldevelopment.spacequest.engine.graphics.swap;

import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.KHRSynchronization2.VK_IMAGE_LAYOUT_ATTACHMENT_OPTIMAL_KHR;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_LOAD_OP_CLEAR;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_STORE;
import static org.lwjgl.vulkan.VK10.VK_BORDER_COLOR_INT_OPAQUE_BLACK;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCmdSetScissor;
import static org.lwjgl.vulkan.VK10.vkCmdSetViewport;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_COLOR_ATTACHMENT_READ_BIT;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_NONE;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_FRAGMENT_SHADER_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_NONE;
import static org.lwjgl.vulkan.VK13.vkCmdBeginRendering;
import static org.lwjgl.vulkan.VK13.vkCmdEndRendering;

import java.nio.LongBuffer;
import java.util.Arrays;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderingAttachmentInfo;
import org.lwjgl.vulkan.VkRenderingInfo;
import org.lwjgl.vulkan.VkViewport;

import com.deepwelldevelopment.spacequest.engine.EngineConfig;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.Attachment;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.CommandBuffer;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.DescAllocator;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.DescSet;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.DescSetLayout;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.Device;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.EmptyVertexBufferStruct;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.Pipeline;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.PipelineBuildInfo;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.ShaderCompiler;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.ShaderModule;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.SwapChain;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.TextureSampler;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.TextureSamplerInfo;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.VulkanContext;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.VulkanUtils;

public class SwapChainRenderer {
    private static final String DESC_ID_ATT = "FWD_DESC_ID_ATT";
    private static final String FRAGMENT_SHADER_FILE_GLSL = "resources/shaders/swap.frag";
    private static final String FRAGMENT_SHADER_FILE_SPV = FRAGMENT_SHADER_FILE_GLSL + ".spv";
    private static final String VERTEX_SHADER_FILE_GLSL = "resources/shaders/swap.vert";
    private static final String VERTEX_SHADER_FILE_SPV = VERTEX_SHADER_FILE_GLSL + ".spv";
    private final DescSetLayout attDescSetLayout;
    private final VkClearValue clrValueColor;
    private final Pipeline pipeline;
    private final TextureSampler textureSampler;
    private VkRenderingAttachmentInfo.Buffer[] colorAttachmentsInfo;
    private VkRenderingInfo[] renderInfo;

    public SwapChainRenderer(VulkanContext vulkanContext, Attachment srcAttachment) {
        clrValueColor = VkClearValue.calloc();
        clrValueColor.color(c -> c.float32(0, 0.0f).float32(1, 0.0f).float32(2, 0.0f).float32(3, 0.0f));

        colorAttachmentsInfo = createColorAttachmentsInfo(vulkanContext, clrValueColor);
        renderInfo = createRenderInfo(vulkanContext, colorAttachmentsInfo);

        var textureSamplerInfo = new TextureSamplerInfo(VK_SAMPLER_ADDRESS_MODE_REPEAT,
                VK_BORDER_COLOR_INT_OPAQUE_BLACK, 1, true);
        textureSampler = new TextureSampler(vulkanContext, textureSamplerInfo);

        var layoutInfo = new DescSetLayout.LayoutInfo(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 0, 1,
                VK_SHADER_STAGE_FRAGMENT_BIT);
        attDescSetLayout = new DescSetLayout(vulkanContext, layoutInfo);
        createAttDescSet(vulkanContext, attDescSetLayout, srcAttachment, textureSampler);

        ShaderModule[] shaderModules = createShaderModules(vulkanContext);

        pipeline = createPipeline(vulkanContext, shaderModules, new DescSetLayout[] { attDescSetLayout });
        Arrays.asList(shaderModules).forEach(s -> s.cleanup(vulkanContext));
    }

    private static void createAttDescSet(VulkanContext vulkanContext, DescSetLayout descSetLayout,
            Attachment attachment, TextureSampler sampler) {
        DescAllocator descAllocator = vulkanContext.getDescAllocator();
        Device device = vulkanContext.getDevice();
        DescSet descSet = descAllocator.addDescSets(device, DESC_ID_ATT, 1, descSetLayout)[0];
        descSet.setImage(device, attachment.getImageView(), sampler, 0);
    }

    private static VkRenderingAttachmentInfo.Buffer[] createColorAttachmentsInfo(VulkanContext vulkanContext,
            VkClearValue clearValue) {
        SwapChain swapChain = vulkanContext.getSwapChain();
        int numImages = swapChain.getNumImages();
        var result = new VkRenderingAttachmentInfo.Buffer[numImages];

        for (int i = 0; i < numImages; ++i) {
            var attachments = VkRenderingAttachmentInfo.calloc(1);
            attachments.get(0).sType$Default().imageView(swapChain.getImageView(i).getVkImageView())
                    .imageLayout(VK_IMAGE_LAYOUT_ATTACHMENT_OPTIMAL_KHR).loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                    .storeOp(VK_ATTACHMENT_STORE_OP_STORE).clearValue(clearValue);
            result[i] = attachments;
        }
        return result;
    }

    private static Pipeline createPipeline(VulkanContext vulkanContext, ShaderModule[] shaderModules,
            DescSetLayout[] descSetLayouts) {
        var vtxBuffStruct = new EmptyVertexBufferStruct();
        var buildInfo = new PipelineBuildInfo(shaderModules, vtxBuffStruct.getVi(),
                vulkanContext.getSurface().getSurfaceFormat().format()).setDescSetLayouts(descSetLayouts);
        var pipeline = new Pipeline(vulkanContext, buildInfo);
        vtxBuffStruct.cleanup();
        return pipeline;
    }

    private static VkRenderingInfo[] createRenderInfo(VulkanContext vulkanContext,
            VkRenderingAttachmentInfo.Buffer[] colorAttachments) {
        SwapChain swapChain = vulkanContext.getSwapChain();
        int numImages = swapChain.getNumImages();
        var result = new VkRenderingInfo[numImages];

        try (var stack = MemoryStack.stackPush()) {
            VkExtent2D extent = swapChain.getSwapChainExtent();
            var renderArea = VkRect2D.calloc(stack).extent(extent);

            for (int i = 0; i < numImages; ++i) {
                var renderingInfo = VkRenderingInfo.calloc().sType$Default().renderArea(renderArea).layerCount(1)
                        .pColorAttachments(colorAttachments[i]);
                result[i] = renderingInfo;
            }
        }
        return result;
    }

    private static ShaderModule[] createShaderModules(VulkanContext vulkanContext) {
        if (EngineConfig.getInstance().isShaderRecompilation()) {
            ShaderCompiler.compileShaderIfChanged(VERTEX_SHADER_FILE_GLSL, Shaderc.shaderc_glsl_vertex_shader);
            ShaderCompiler.compileShaderIfChanged(FRAGMENT_SHADER_FILE_GLSL, Shaderc.shaderc_glsl_fragment_shader);
        }
        return new ShaderModule[] {
                new ShaderModule(vulkanContext, VK_SHADER_STAGE_VERTEX_BIT, VERTEX_SHADER_FILE_SPV, null),
                new ShaderModule(vulkanContext, VK_SHADER_STAGE_FRAGMENT_BIT, FRAGMENT_SHADER_FILE_SPV, null), };
    }

    public void cleanup(VulkanContext vulkanContext) {
        textureSampler.cleanup(vulkanContext);
        attDescSetLayout.cleanup(vulkanContext);
        pipeline.cleanup(vulkanContext);
        Arrays.asList(renderInfo).forEach(VkRenderingInfo::free);
        Arrays.asList(colorAttachmentsInfo).forEach(VkRenderingAttachmentInfo.Buffer::free);
        clrValueColor.free();
    }

    public void render(VulkanContext vulkanContext, CommandBuffer cmdBuffer, Attachment srcAttachment, int imageIndex) {
        try (var stack = MemoryStack.stackPush()) {
            SwapChain swapChain = vulkanContext.getSwapChain();

            long swapChainImage = swapChain.getImageView(imageIndex).getVkImage();
            VkCommandBuffer cmdHandle = cmdBuffer.getVkCommandBuffer();

            VulkanUtils.imageBarrier(stack, cmdHandle, swapChainImage, VK_IMAGE_LAYOUT_UNDEFINED,
                    VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT,
                    VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT, VK_ACCESS_2_NONE,
                    VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT, VK_IMAGE_ASPECT_COLOR_BIT);

            VulkanUtils.imageBarrier(stack, cmdHandle, srcAttachment.getImage().getVkImage(),
                    VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                    VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT, VK_PIPELINE_STAGE_2_FRAGMENT_SHADER_BIT,
                    VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT, VK_ACCESS_2_SHADER_READ_BIT, VK_IMAGE_ASPECT_COLOR_BIT);

            vkCmdBeginRendering(cmdHandle, renderInfo[imageIndex]);

            vkCmdBindPipeline(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.getVkPipeline());

            VkExtent2D swapChainExtent = swapChain.getSwapChainExtent();
            int width = swapChainExtent.width();
            int height = swapChainExtent.height();
            var viewport = VkViewport.calloc(1, stack).x(0).y(height).height(-height).width(width).minDepth(0.0f)
                    .maxDepth(1.0f);
            vkCmdSetViewport(cmdHandle, 0, viewport);

            var scissor = VkRect2D.calloc(1, stack).extent(it -> it.width(width).height(height))
                    .offset(it -> it.x(0).y(0));
            vkCmdSetScissor(cmdHandle, 0, scissor);

            DescAllocator descAllocator = vulkanContext.getDescAllocator();
            LongBuffer descriptorSets = stack.mallocLong(1).put(0,
                    descAllocator.getDescSet(DESC_ID_ATT).getVkDescriptorSet());
            vkCmdBindDescriptorSets(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.getVkPipelineLayout(), 0,
                    descriptorSets, null);

            vkCmdDraw(cmdHandle, 3, 1, 0, 0);

            vkCmdEndRendering(cmdHandle);

            VulkanUtils.imageBarrier(stack, cmdHandle, swapChainImage, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                    VK_IMAGE_LAYOUT_PRESENT_SRC_KHR, VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT,
                    VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT,
                    VK_ACCESS_2_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT,
                    VK_PIPELINE_STAGE_2_NONE, VK_IMAGE_ASPECT_COLOR_BIT);
        }
    }

    public void resize(VulkanContext vulkanContext, Attachment srcAttachment) {
        Arrays.asList(renderInfo).forEach(VkRenderingInfo::free);
        Arrays.asList(colorAttachmentsInfo).forEach(VkRenderingAttachmentInfo.Buffer::free);
        colorAttachmentsInfo = createColorAttachmentsInfo(vulkanContext, clrValueColor);
        renderInfo = createRenderInfo(vulkanContext, colorAttachmentsInfo);

        DescAllocator descAllocator = vulkanContext.getDescAllocator();
        DescSet descSet = descAllocator.getDescSet(DESC_ID_ATT);
        descSet.setImage(vulkanContext.getDevice(), srcAttachment.getImageView(), textureSampler, 0);
    }
}
