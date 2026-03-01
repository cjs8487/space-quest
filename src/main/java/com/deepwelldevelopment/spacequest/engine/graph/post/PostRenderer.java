package com.deepwelldevelopment.spacequest.engine.graph.post;

import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_LOAD_OP_CLEAR;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_STORE;
import static org.lwjgl.vulkan.VK10.VK_BORDER_COLOR_INT_OPAQUE_BLACK;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16G16B16A16_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCmdSetScissor;
import static org.lwjgl.vulkan.VK10.vkCmdSetViewport;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_NONE;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_FRAGMENT_SHADER_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK13.vkCmdBeginRendering;
import static org.lwjgl.vulkan.VK13.vkCmdEndRendering;

import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderingAttachmentInfo;
import org.lwjgl.vulkan.VkRenderingInfo;
import org.lwjgl.vulkan.VkViewport;

import com.deepwelldevelopment.spacequest.engine.EngineConfig;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Attachment;
import com.deepwelldevelopment.spacequest.engine.graph.vk.CommandBuffer;
import com.deepwelldevelopment.spacequest.engine.graph.vk.DescAllocator;
import com.deepwelldevelopment.spacequest.engine.graph.vk.DescSet;
import com.deepwelldevelopment.spacequest.engine.graph.vk.DescSetLayout;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Device;
import com.deepwelldevelopment.spacequest.engine.graph.vk.EmptyVertexBufferStruct;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Image;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Pipeline;
import com.deepwelldevelopment.spacequest.engine.graph.vk.PipelineBuildInfo;
import com.deepwelldevelopment.spacequest.engine.graph.vk.ShaderCompiler;
import com.deepwelldevelopment.spacequest.engine.graph.vk.ShaderModule;
import com.deepwelldevelopment.spacequest.engine.graph.vk.SwapChain;
import com.deepwelldevelopment.spacequest.engine.graph.vk.TextureSampler;
import com.deepwelldevelopment.spacequest.engine.graph.vk.TextureSamplerInfo;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanBuffer;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanContext;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanUtils;

public class PostRenderer {

    public static final int COLOR_FORMAT = VK_FORMAT_R16G16B16A16_SFLOAT;
    private static final String DESC_ID_ATT = "POST_DESC_ID_ATT";
    private static final String DESC_ID_SCREEN_SIZE = "POST_DESC_ID_SCREEN_SIZE";
    private static final String FRAGMENT_SHADER_FILE_GLSL = "resources/shaders/post.frag";
    private static final String FRAGMENT_SHADER_FILE_SPV = FRAGMENT_SHADER_FILE_GLSL + ".spv";
    private static final String VERTEX_SHADER_FILE_GLSL = "resources/shaders/post.vert";
    private static final String VERTEX_SHADER_FILE_SPV = VERTEX_SHADER_FILE_GLSL + ".spv";

    private final DescSetLayout attDescSetLayout;
    private final VkClearValue clrValueColor;
    private final DescSetLayout frgUniformDescSetLayout;
    private final Pipeline pipeline;
    private final VulkanBuffer scrSizeBuff;
    private final SpecConstants specConstants;
    private final TextureSampler textureSampler;
    private Attachment colorAttachment;
    private VkRenderingAttachmentInfo.Buffer colorAttachmentInfo;
    private VkRenderingInfo renderInfo;

    public PostRenderer(VulkanContext vulkanContext, Attachment srcAttachment) {
        clrValueColor = VkClearValue.calloc();
        clrValueColor.color(c -> c.float32(0, 0.0f).float32(1, 0.0f).float32(2, 0.0f).float32(3, 0.0f));

        colorAttachment = createColorAttachment(vulkanContext);
        colorAttachmentInfo = createColorAttachmentInfo(colorAttachment, clrValueColor);
        renderInfo = createRenderInfo(colorAttachment, colorAttachmentInfo);

        var textureSamplerInfo = new TextureSamplerInfo(VK_SAMPLER_ADDRESS_MODE_REPEAT,
                VK_BORDER_COLOR_INT_OPAQUE_BLACK, 1, true);
        textureSampler = new TextureSampler(vulkanContext, textureSamplerInfo);

        var layoutInfo = new DescSetLayout.LayoutInfo(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 0, 1,
                VK_SHADER_STAGE_FRAGMENT_BIT);
        attDescSetLayout = new DescSetLayout(vulkanContext, layoutInfo);
        createAttDescSet(vulkanContext, attDescSetLayout, srcAttachment, textureSampler);

        layoutInfo = new DescSetLayout.LayoutInfo(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 0, 1,
                VK_SHADER_STAGE_FRAGMENT_BIT);
        frgUniformDescSetLayout = new DescSetLayout(vulkanContext, layoutInfo);
        scrSizeBuff = VulkanUtils.createHostVisibleBuff(vulkanContext, VulkanUtils.VEC2_SIZE,
                VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, DESC_ID_SCREEN_SIZE, frgUniformDescSetLayout);
        setScrSizeBuffer(vulkanContext);

        specConstants = new SpecConstants();
        ShaderModule[] shaderModules = createShaderModules(vulkanContext, specConstants);

        pipeline = createPipeline(vulkanContext, shaderModules,
                new DescSetLayout[] { attDescSetLayout, frgUniformDescSetLayout });
        Arrays.asList(shaderModules).forEach(s -> s.cleanup(vulkanContext));
    }

    private static void createAttDescSet(VulkanContext vulkanContext, DescSetLayout descSetLayout,
            Attachment attachment, TextureSampler sampler) {
        DescAllocator descAllocator = vulkanContext.getDescAllocator();
        Device device = vulkanContext.getDevice();
        DescSet descSet = descAllocator.addDescSets(device, DESC_ID_ATT, 1, descSetLayout)[0];
        descSet.setImage(device, attachment.getImageView(), sampler, 0);
    }

    private static Attachment createColorAttachment(VulkanContext vulkanContext) {
        SwapChain swapChain = vulkanContext.getSwapChain();
        VkExtent2D swapChainExtent = swapChain.getSwapChainExtent();
        return new Attachment(vulkanContext, swapChainExtent.width(), swapChainExtent.height(), COLOR_FORMAT,
                VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
    }

    private static VkRenderingAttachmentInfo.Buffer createColorAttachmentInfo(Attachment srcAttachment,
            VkClearValue clearValue) {
        return VkRenderingAttachmentInfo.calloc(1).sType$Default()
                .imageView(srcAttachment.getImageView().getVkImageView())
                .imageLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL).loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                .storeOp(VK_ATTACHMENT_STORE_OP_STORE).clearValue(clearValue);
    }

    private static Pipeline createPipeline(VulkanContext vulkanContext, ShaderModule[] shaderModules,
            DescSetLayout[] descSetLayouts) {
        var vtxBuffStruct = new EmptyVertexBufferStruct();
        var buildInfo = new PipelineBuildInfo(shaderModules, vtxBuffStruct.getVi(), COLOR_FORMAT)
                .setDescSetLayouts(descSetLayouts);
        var pipeline = new Pipeline(vulkanContext, buildInfo);
        vtxBuffStruct.cleanup();
        return pipeline;
    }

    private static VkRenderingInfo createRenderInfo(Attachment colorAttachment,
            VkRenderingAttachmentInfo.Buffer colorAttachmentInfo) {
        VkRenderingInfo renderingInfo;
        try (var stack = MemoryStack.stackPush()) {
            Image image = colorAttachment.getImage();
            VkExtent2D extent = VkExtent2D.calloc(stack).width(image.getWidth()).height(image.getHeight());
            var renderArea = VkRect2D.calloc(stack).extent(extent);

            renderingInfo = VkRenderingInfo.calloc().sType$Default().renderArea(renderArea).layerCount(1)
                    .pColorAttachments(colorAttachmentInfo);
        }
        return renderingInfo;
    }

    private static ShaderModule[] createShaderModules(VulkanContext vulkanContext, SpecConstants specConstants) {
        if (EngineConfig.getInstance().isShaderRecompilation()) {
            ShaderCompiler.compileShaderIfChanged(VERTEX_SHADER_FILE_GLSL, Shaderc.shaderc_glsl_vertex_shader);
            ShaderCompiler.compileShaderIfChanged(FRAGMENT_SHADER_FILE_GLSL, Shaderc.shaderc_glsl_fragment_shader);
        }
        return new ShaderModule[] {
                new ShaderModule(vulkanContext, VK_SHADER_STAGE_VERTEX_BIT, VERTEX_SHADER_FILE_SPV, null),
                new ShaderModule(vulkanContext, VK_SHADER_STAGE_FRAGMENT_BIT, FRAGMENT_SHADER_FILE_SPV,
                        specConstants.getSpecInfo()), };
    }

    public void cleanup(VulkanContext vulkanContext) {
        clrValueColor.free();
        colorAttachment.cleanup(vulkanContext);
        textureSampler.cleanup(vulkanContext);
        attDescSetLayout.cleanup(vulkanContext);
        frgUniformDescSetLayout.cleanup(vulkanContext);
        pipeline.cleanup(vulkanContext);
        renderInfo.free();
        colorAttachmentInfo.free();
        scrSizeBuff.cleanup(vulkanContext);
        specConstants.cleanup();
    }

    public Attachment getAttachment() {
        return colorAttachment;
    }

    public void render(VulkanContext vulkanContext, CommandBuffer cmdBuffer, Attachment srcAttachment) {
        try (var stack = MemoryStack.stackPush()) {
            SwapChain swapChain = vulkanContext.getSwapChain();

            VkCommandBuffer cmdHandle = cmdBuffer.getVkCommandBuffer();

            VulkanUtils.imageBarrier(stack, cmdHandle, srcAttachment.getImage().getVkImage(),
                    VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                    VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT, VK_PIPELINE_STAGE_2_FRAGMENT_SHADER_BIT,
                    VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT, VK_ACCESS_2_SHADER_READ_BIT, VK_IMAGE_ASPECT_COLOR_BIT);

            VulkanUtils.imageBarrier(stack, cmdHandle, colorAttachment.getImage().getVkImage(),
                    VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                    VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT, VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT,
                    VK_ACCESS_2_NONE, VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT, VK_IMAGE_ASPECT_COLOR_BIT);

            vkCmdBeginRendering(cmdHandle, renderInfo);

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
            LongBuffer descriptorSets = stack.mallocLong(2)
                    .put(0, descAllocator.getDescSet(DESC_ID_ATT).getVkDescriptorSet())
                    .put(1, descAllocator.getDescSet(DESC_ID_SCREEN_SIZE).getVkDescriptorSet());
            vkCmdBindDescriptorSets(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.getVkPipelineLayout(), 0,
                    descriptorSets, null);

            vkCmdDraw(cmdHandle, 3, 1, 0, 0);

            vkCmdEndRendering(cmdHandle);
        }
    }

    public void resize(VulkanContext vulkanContext, Attachment srcAttachment) {
        renderInfo.free();
        colorAttachment.cleanup(vulkanContext);
        colorAttachmentInfo.free();
        colorAttachment = createColorAttachment(vulkanContext);
        colorAttachmentInfo = createColorAttachmentInfo(colorAttachment, clrValueColor);
        renderInfo = createRenderInfo(colorAttachment, colorAttachmentInfo);

        DescAllocator descAllocator = vulkanContext.getDescAllocator();
        DescSet descSet = descAllocator.getDescSet(DESC_ID_ATT);
        descSet.setImage(vulkanContext.getDevice(), srcAttachment.getImageView(), textureSampler, 0);

        setScrSizeBuffer(vulkanContext);
    }

    private void setScrSizeBuffer(VulkanContext vulkanContext) {
        long mappedMemory = scrSizeBuff.map(vulkanContext);
        FloatBuffer dataBuff = MemoryUtil.memFloatBuffer(mappedMemory, (int) scrSizeBuff.getRequestedSize());
        VkExtent2D swapChainExtent = vulkanContext.getSwapChain().getSwapChainExtent();
        dataBuff.put(0, swapChainExtent.width());
        dataBuff.put(1, swapChainExtent.height());
        scrSizeBuff.unmap(vulkanContext);
    }
}
