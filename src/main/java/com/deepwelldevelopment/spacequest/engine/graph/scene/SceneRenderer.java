package com.deepwelldevelopment.spacequest.engine.graph.scene;

import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.KHRSynchronization2.VK_IMAGE_LAYOUT_ATTACHMENT_OPTIMAL_KHR;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_LOAD_OP_CLEAR;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_STORE;
import static org.lwjgl.vulkan.VK10.VK_BORDER_COLOR_INT_OPAQUE_BLACK;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D16_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_DEPTH_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_INDEX_TYPE_UINT32;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindIndexBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDrawIndexed;
import static org.lwjgl.vulkan.VK10.vkCmdPushConstants;
import static org.lwjgl.vulkan.VK10.vkCmdSetScissor;
import static org.lwjgl.vulkan.VK10.vkCmdSetViewport;
import static org.lwjgl.vulkan.VK12.VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_COLOR_ATTACHMENT_READ_BIT;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_READ_BIT;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_NONE;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_NONE;
import static org.lwjgl.vulkan.VK13.vkCmdBeginRendering;
import static org.lwjgl.vulkan.VK13.vkCmdEndRendering;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.List;

import org.joml.Matrix4f;
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
import org.tinylog.Logger;

import com.deepwelldevelopment.spacequest.engine.EngineConfig;
import com.deepwelldevelopment.spacequest.engine.EngineContext;
import com.deepwelldevelopment.spacequest.engine.graph.MaterialsCache;
import com.deepwelldevelopment.spacequest.engine.graph.ModelsCache;
import com.deepwelldevelopment.spacequest.engine.graph.TextureCache;
import com.deepwelldevelopment.spacequest.engine.graph.VulkanMaterial;
import com.deepwelldevelopment.spacequest.engine.graph.VulkanMesh;
import com.deepwelldevelopment.spacequest.engine.graph.VulkanModel;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Attachment;
import com.deepwelldevelopment.spacequest.engine.graph.vk.CommandBuffer;
import com.deepwelldevelopment.spacequest.engine.graph.vk.DescAllocator;
import com.deepwelldevelopment.spacequest.engine.graph.vk.DescSet;
import com.deepwelldevelopment.spacequest.engine.graph.vk.DescSetLayout;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Device;
import com.deepwelldevelopment.spacequest.engine.graph.vk.ImageView;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Pipeline;
import com.deepwelldevelopment.spacequest.engine.graph.vk.PipelineBuildInfo;
import com.deepwelldevelopment.spacequest.engine.graph.vk.PushConstRange;
import com.deepwelldevelopment.spacequest.engine.graph.vk.ShaderCompiler;
import com.deepwelldevelopment.spacequest.engine.graph.vk.ShaderModule;
import com.deepwelldevelopment.spacequest.engine.graph.vk.SwapChain;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Texture;
import com.deepwelldevelopment.spacequest.engine.graph.vk.TextureSampler;
import com.deepwelldevelopment.spacequest.engine.graph.vk.TextureSamplerInfo;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanBuffer;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanContext;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanUtils;
import com.deepwelldevelopment.spacequest.engine.scene.Entity;
import com.deepwelldevelopment.spacequest.engine.scene.Scene;

public class SceneRenderer {

    private static final int DEPTH_FORMAT = VK_FORMAT_D16_UNORM;
    private static final String DESC_ID_MAT = "SCN_DESC_ID_MAT";
    private static final String DESC_ID_PRJ = "SCN_DESC_ID_PRJ";
    private static final String DESC_ID_TEXT = "SCN_DESC_ID_TEXT";

    private static final int PUSH_CONSTANTS_SIZE = VulkanUtils.MAT4X4_SIZE + VulkanUtils.INT_SIZE;

    private static final String FRAGMENT_SHADER_FILE_GLSL = "resources/shaders/scn_frg.glsl";
    private static final String FRAGMENT_SHADER_FILE_SPV = FRAGMENT_SHADER_FILE_GLSL + ".spv";
    private static final String VERTEX_SHADER_FILE_GLSL = "resources/shaders/scn_vtx.glsl";
    private static final String VERTEX_SHADER_FILE_SPV = VERTEX_SHADER_FILE_GLSL + ".spv";

    private final VulkanBuffer buffProjMatrix;
    private final VkClearValue clearValueColor;
    private final VkClearValue clrValueDepth;
    private final DescSetLayout descLayoutFrgStorage;
    private final DescSetLayout descLayoutTexture;
    private final DescSetLayout descLayoutVtxUniform;
    private final Pipeline pipeline;
    private final ByteBuffer pushConstBuff;
    private final TextureSampler textureSampler;
    private Attachment[] attDepth;
    private VkRenderingAttachmentInfo.Buffer[] attInfoColor;
    private VkRenderingAttachmentInfo[] attInfoDepth;
    private VkRenderingInfo[] renderInfo;

    public SceneRenderer(VulkanContext vulkanContext, EngineContext engineContext) {
        clearValueColor = VkClearValue.calloc()
                .color(c -> c.float32(0, 0.0f).float32(1, 0.0f).float32(2, 0.0f).float32(3, 0.0f));
        clrValueDepth = VkClearValue.calloc().color(c -> c.float32(0, 1.0f));
        attDepth = createDepthAttachments(vulkanContext);
        attInfoColor = createColorAttachmentsInfo(vulkanContext, clearValueColor);
        attInfoDepth = createDepthAttachmentsInfo(vulkanContext, attDepth, clrValueDepth);
        renderInfo = createRenderInfo(vulkanContext, attInfoColor, attInfoDepth);

        ShaderModule[] shaderModules = createShaderModules(vulkanContext);

        pushConstBuff = MemoryUtil.memAlloc(PUSH_CONSTANTS_SIZE);

        descLayoutVtxUniform = new DescSetLayout(vulkanContext,
                new DescSetLayout.LayoutInfo(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                        0, 1, VK_SHADER_STAGE_VERTEX_BIT));
        buffProjMatrix = VulkanUtils.createHostVisibleBuff(vulkanContext, VulkanUtils.MAT4X4_SIZE,
                VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                DESC_ID_PRJ, descLayoutVtxUniform);
        VulkanUtils.copyMatrixToBuffer(vulkanContext, buffProjMatrix,
                engineContext.scene().getProjection().getProjectionMatrix(), 0);

        descLayoutFrgStorage = new DescSetLayout(vulkanContext,
                new DescSetLayout.LayoutInfo(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER,
                        0, 1, VK_SHADER_STAGE_FRAGMENT_BIT));

        var textureSamplerInfo = new TextureSamplerInfo(VK_SAMPLER_ADDRESS_MODE_REPEAT,
                VK_BORDER_COLOR_INT_OPAQUE_BLACK, 1, true);
        textureSampler = new TextureSampler(vulkanContext, textureSamplerInfo);
        descLayoutTexture = new DescSetLayout(vulkanContext,
                new DescSetLayout.LayoutInfo(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                        0, TextureCache.MAX_TEXTURES, VK_SHADER_STAGE_FRAGMENT_BIT));

        pipeline = createPipeline(vulkanContext, shaderModules,
                new DescSetLayout[] { descLayoutVtxUniform, descLayoutFrgStorage, descLayoutTexture });
        Arrays.asList(shaderModules).forEach(s -> s.cleanup(vulkanContext));
    }

    private static VkRenderingAttachmentInfo.Buffer[] createColorAttachmentsInfo(VulkanContext context,
            VkClearValue clearValue) {
        SwapChain swapChain = context.getSwapChain();
        int numImages = swapChain.getNumImages();
        var result = new VkRenderingAttachmentInfo.Buffer[numImages];

        for (int i = 0; i < numImages; ++i) {
            var attachments = VkRenderingAttachmentInfo.calloc(1)
                    .sType$Default()
                    .imageView(swapChain.getImageView(i).getVkImageView())
                    .imageLayout(VK_IMAGE_LAYOUT_ATTACHMENT_OPTIMAL_KHR)
                    .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                    .storeOp(VK_ATTACHMENT_STORE_OP_STORE)
                    .clearValue(clearValue);
            result[i] = attachments;
        }
        return result;
    }

    private static VkRenderingInfo[] createRenderInfo(VulkanContext context,
            VkRenderingAttachmentInfo.Buffer[] colorAttachments, VkRenderingAttachmentInfo[] depthAttachments) {
        SwapChain swapChain = context.getSwapChain();
        int numImages = swapChain.getNumImages();
        var result = new VkRenderingInfo[numImages];

        try (var stack = MemoryStack.stackPush()) {
            VkExtent2D extent = swapChain.getSwapChainExtent();
            var renderArea = VkRect2D.calloc(stack).extent(extent);

            for (int i = 0; i < numImages; ++i) {
                var renderingInfo = VkRenderingInfo.calloc()
                        .sType$Default()
                        .renderArea(renderArea)
                        .layerCount(1)
                        .pColorAttachments(colorAttachments[i])
                        .pDepthAttachment(depthAttachments[i]);
                result[i] = renderingInfo;
            }
        }
        return result;
    }

    private static Attachment[] createDepthAttachments(VulkanContext context) {
        SwapChain swapChain = context.getSwapChain();
        int numImages = swapChain.getNumImages();
        VkExtent2D swapChainExtent = swapChain.getSwapChainExtent();
        Attachment[] depthAttachments = new Attachment[numImages];
        for (int i = 0; i < numImages; i++) {
            depthAttachments[i] = new Attachment(context, swapChainExtent.width(), swapChainExtent.height(),
                    DEPTH_FORMAT, VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT);
        }

        return depthAttachments;
    }

    private static VkRenderingAttachmentInfo[] createDepthAttachmentsInfo(VulkanContext context,
            Attachment[] depthAttachments,
            VkClearValue clearValue) {
        SwapChain swapChain = context.getSwapChain();
        int numImages = swapChain.getNumImages();
        var result = new VkRenderingAttachmentInfo[numImages];

        for (int i = 0; i < numImages; ++i) {
            var attachments = VkRenderingAttachmentInfo.calloc()
                    .sType$Default()
                    .imageView(depthAttachments[i].getImageView().getVkImageView())
                    .imageLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
                    .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                    .storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
                    .clearValue(clearValue);
            result[i] = attachments;
        }
        return result;
    }

    private static Pipeline createPipeline(VulkanContext vkCtx, ShaderModule[] shaderModules,
            DescSetLayout[] descSetLayouts) {
        var vtxBuffStruct = new VertexBufferStruct();
        var buildInfo = new PipelineBuildInfo(shaderModules, vtxBuffStruct.getVi(),
                vkCtx.getSurface().getSurfaceFormat().format())
                .setDepthFormat(DEPTH_FORMAT)
                .setPushConstRanges(
                        new PushConstRange[] {
                                new PushConstRange(VK_SHADER_STAGE_VERTEX_BIT, 0, VulkanUtils.MAT4X4_SIZE),
                                new PushConstRange(VK_SHADER_STAGE_FRAGMENT_BIT, VulkanUtils.MAT4X4_SIZE,
                                        VulkanUtils.INT_SIZE),
                        })
                .setDescSetLayouts(descSetLayouts);
        var pipeline = new Pipeline(vkCtx, buildInfo);
        vtxBuffStruct.cleanup();
        return pipeline;
    }

    private static ShaderModule[] createShaderModules(VulkanContext context) {
        if (EngineConfig.getInstance().isShaderRecompilation()) {
            ShaderCompiler.compileShaderIfChanged(VERTEX_SHADER_FILE_GLSL, Shaderc.shaderc_glsl_vertex_shader);
            ShaderCompiler.compileShaderIfChanged(FRAGMENT_SHADER_FILE_GLSL, Shaderc.shaderc_glsl_fragment_shader);
        }
        return new ShaderModule[] {
                new ShaderModule(context, VK_SHADER_STAGE_VERTEX_BIT, VERTEX_SHADER_FILE_SPV),
                new ShaderModule(context, VK_SHADER_STAGE_FRAGMENT_BIT, FRAGMENT_SHADER_FILE_SPV),
        };
    }

    public void cleanup(VulkanContext context) {
        pipeline.cleanup(context);
        Arrays.asList(renderInfo).forEach(VkRenderingInfo::free);
        Arrays.asList(attInfoColor).forEach(VkRenderingAttachmentInfo.Buffer::free);
        clearValueColor.free();
    }

    public void loadMaterials(VulkanContext vkCtx, MaterialsCache materialsCache, TextureCache textureCache) {
        DescAllocator descAllocator = vkCtx.getDescAllocator();
        Device device = vkCtx.getDevice();
        DescSet descSet = descAllocator.addDescSet(device, DESC_ID_MAT, descLayoutFrgStorage);
        DescSetLayout.LayoutInfo layoutInfo = descLayoutFrgStorage.getLayoutInfo();
        var buffer = materialsCache.getMaterialsBuffer();
        descSet.setBuffer(device, buffer, buffer.getRequestedSize(), layoutInfo.binding(), layoutInfo.descType());

        List<ImageView> imageViews = textureCache.getAsList().stream().map(Texture::getImageView).toList();
        descSet = vkCtx.getDescAllocator().addDescSet(device, DESC_ID_TEXT, descLayoutTexture);
        descSet.setImagesArr(device, imageViews, textureSampler, 0);
    }

    public void render(EngineContext engineContext, VulkanContext vulkanContext, CommandBuffer buffer,
            ModelsCache modelsCache, MaterialsCache materialsCache, int imageIndex) {
        try (var stack = MemoryStack.stackPush()) {
            SwapChain swapChain = vulkanContext.getSwapChain();

            long swapChainImage = swapChain.getImageView(imageIndex).getVkImage();
            VkCommandBuffer cmdHandle = buffer.getVkCommandBuffer();

            VulkanUtils.imageBarrier(stack, cmdHandle, swapChainImage,
                    VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                    VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT, VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT,
                    VK_ACCESS_2_NONE, VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT,
                    VK_IMAGE_ASPECT_COLOR_BIT);
            VulkanUtils.imageBarrier(stack, cmdHandle, attDepth[imageIndex].getImage().getVkImage(),
                    VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL,
                    VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT | VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT,
                    VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT | VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT,
                    VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT,
                    VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT,
                    VK_IMAGE_ASPECT_DEPTH_BIT);

            vkCmdBeginRendering(cmdHandle, renderInfo[imageIndex]);

            vkCmdBindPipeline(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.getVkPipeline());

            VkExtent2D swapChainExtent = swapChain.getSwapChainExtent();
            int width = swapChainExtent.width();
            int height = swapChainExtent.height();
            var viewport = VkViewport.calloc(1, stack)
                    .x(0)
                    .y(height)
                    .height(-height)
                    .width(width)
                    .minDepth(0.0f)
                    .maxDepth(1.0f);
            vkCmdSetViewport(cmdHandle, 0, viewport);

            var scissor = VkRect2D.calloc(1, stack)
                    .extent(it -> it.width(width).height(height))
                    .offset(it -> it.x(0).y(0));
            vkCmdSetScissor(cmdHandle, 0, scissor);

            LongBuffer offsets = stack.mallocLong(1).put(0, 0L);
            LongBuffer vertexBuffer = stack.mallocLong(1);

            DescAllocator descAllocator = vulkanContext.getDescAllocator();
            LongBuffer descriptorSets = stack.mallocLong(3)
                    .put(0, descAllocator.getDescSet(DESC_ID_PRJ).getVkDescriptorSet())
                    .put(1, descAllocator.getDescSet(DESC_ID_MAT).getVkDescriptorSet())
                    .put(2, descAllocator.getDescSet(DESC_ID_TEXT).getVkDescriptorSet());
            vkCmdBindDescriptorSets(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.getVkPipelineLayout(),
                    0, descriptorSets, null);

            Scene scene = engineContext.scene();
            List<Entity> entities = scene.getEntities();
            int numEntities = entities.size();
            for (int i = 0; i < numEntities; i++) {
                var entity = entities.get(i);
                VulkanModel model = modelsCache.getModel(entity.getModelId());
                List<VulkanMesh> vulkanMeshList = model.getMeshes();
                int numMeshes = vulkanMeshList.size();
                for (int j = 0; j < numMeshes; j++) {
                    var vulkanMesh = vulkanMeshList.get(j);
                    String materialId = vulkanMesh.materialId();
                    int materialIdx = materialsCache.getPosition(materialId);
                    VulkanMaterial vulkanMaterial = materialsCache.getMaterial(materialId);
                    if (vulkanMaterial == null) {
                        Logger.warn("Mesh [{}] in model [{}] does not have material", j, model.getId());
                        continue;
                    }
                    setPushConstants(cmdHandle, entity.getModelMatrix(), materialIdx);
                    vertexBuffer.put(0, vulkanMesh.verticesBuffer().getBuffer());
                    vkCmdBindVertexBuffers(cmdHandle, 0, vertexBuffer, offsets);
                    vkCmdBindIndexBuffer(cmdHandle, vulkanMesh.indicesBuffer().getBuffer(), 0, VK_INDEX_TYPE_UINT32);
                    vkCmdDrawIndexed(cmdHandle, vulkanMesh.numIndices(), 1, 0, 0, 0);
                }
            }

            vkCmdEndRendering(cmdHandle);

            VulkanUtils.imageBarrier(stack, cmdHandle, swapChainImage,
                    VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK_IMAGE_LAYOUT_PRESENT_SRC_KHR,
                    VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT, VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT,
                    VK_ACCESS_2_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT,
                    VK_PIPELINE_STAGE_2_NONE,
                    VK_IMAGE_ASPECT_COLOR_BIT);
        }
    }

    public void resize(EngineContext engineContext, VulkanContext vkCtx) {
        Arrays.asList(renderInfo).forEach(VkRenderingInfo::free);
        Arrays.asList(attInfoDepth).forEach(VkRenderingAttachmentInfo::free);
        Arrays.asList(attInfoColor).forEach(VkRenderingAttachmentInfo.Buffer::free);
        Arrays.asList(attDepth).forEach(a -> a.cleanup(vkCtx));
        attDepth = createDepthAttachments(vkCtx);
        attInfoColor = createColorAttachmentsInfo(vkCtx, clearValueColor);
        attInfoDepth = createDepthAttachmentsInfo(vkCtx, attDepth, clrValueDepth);
        renderInfo = createRenderInfo(vkCtx, attInfoColor, attInfoDepth);
        VulkanUtils.copyMatrixToBuffer(vkCtx, buffProjMatrix,
                engineContext.scene().getProjection().getProjectionMatrix(), 0);
    }

    private void setPushConstants(VkCommandBuffer cmdHandle, Matrix4f modelMatrix, int materialIndex) {
        modelMatrix.get(0, pushConstBuff);
        pushConstBuff.putInt(VulkanUtils.MAT4X4_SIZE, materialIndex);
        vkCmdPushConstants(cmdHandle, pipeline.getVkPipelineLayout(), VK_SHADER_STAGE_VERTEX_BIT, 0,
                pushConstBuff.slice(0, VulkanUtils.MAT4X4_SIZE));
        vkCmdPushConstants(cmdHandle, pipeline.getVkPipelineLayout(), VK_SHADER_STAGE_FRAGMENT_BIT,
                VulkanUtils.MAT4X4_SIZE,
                pushConstBuff.slice(VulkanUtils.MAT4X4_SIZE, VulkanUtils.INT_SIZE));
    }
}
