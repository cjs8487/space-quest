package com.deepwelldevelopment.spacequest.engine.graphics.scene;

import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_LOAD_OP_CLEAR;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_STORE;
import static org.lwjgl.vulkan.VK10.VK_BORDER_COLOR_INT_OPAQUE_BLACK;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16G16B16A16_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_DEPTH_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
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
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_READ_BIT;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_NONE;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT;
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
import com.deepwelldevelopment.spacequest.engine.graphics.MaterialsCache;
import com.deepwelldevelopment.spacequest.engine.graphics.ModelsCache;
import com.deepwelldevelopment.spacequest.engine.graphics.TextureCache;
import com.deepwelldevelopment.spacequest.engine.graphics.VulkanMaterial;
import com.deepwelldevelopment.spacequest.engine.graphics.VulkanMesh;
import com.deepwelldevelopment.spacequest.engine.graphics.VulkanModel;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.Attachment;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.CommandBuffer;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.DescAllocator;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.DescSet;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.DescSetLayout;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.Device;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.Image;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.ImageView;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.Pipeline;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.PipelineBuildInfo;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.PushConstRange;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.ShaderCompiler;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.ShaderModule;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.SwapChain;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.Texture;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.TextureSampler;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.TextureSamplerInfo;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.VulkanBuffer;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.VulkanContext;
import com.deepwelldevelopment.spacequest.engine.graphics.vk.VulkanUtils;
import com.deepwelldevelopment.spacequest.engine.scene.Entity;
import com.deepwelldevelopment.spacequest.engine.scene.Scene;

public class SceneRenderer {

    private static final int DEPTH_FORMAT = VK_FORMAT_D32_SFLOAT;
    private static final int COLOR_FORMAT = VK_FORMAT_R16G16B16A16_SFLOAT;
    private static final String DESC_ID_MAT = "SCN_DESC_ID_MAT";
    private static final String DESC_ID_PRJ = "SCN_DESC_ID_PRJ";
    private static final String DESC_ID_TEXT = "SCN_DESC_ID_TEXT";
    private static final String DESC_ID_VIEW = "SCN_DESC_ID_VIEW";
    private static final String FRAGMENT_SHADER_FILE_GLSL = "resources/shaders/scn_frg.glsl";
    private static final String FRAGMENT_SHADER_FILE_SPV = FRAGMENT_SHADER_FILE_GLSL + ".spv";
    private static final int PUSH_CONSTANTS_SIZE = VulkanUtils.MAT4X4_SIZE + VulkanUtils.INT_SIZE;
    private static final String VERTEX_SHADER_FILE_GLSL = "resources/shaders/scn_vtx.glsl";
    private static final String VERTEX_SHADER_FILE_SPV = VERTEX_SHADER_FILE_GLSL + ".spv";

    private final VulkanBuffer buffProjMatrix;
    private final VulkanBuffer[] buffViewMatrices;
    private final VkClearValue clrValueColor;
    private final VkClearValue clrValueDepth;
    private final DescSetLayout descLayoutFrgStorage;
    private final DescSetLayout descLayoutTexture;
    private final DescSetLayout descLayoutVtxUniform;
    private final Pipeline pipeline;
    private final ByteBuffer pushConstBuff;
    private final TextureSampler textureSampler;
    private Attachment attColor;
    private Attachment attDepth;
    private VkRenderingAttachmentInfo.Buffer attInfoColor;
    private VkRenderingAttachmentInfo attInfoDepth;
    private VkRenderingInfo renderInfo;

    public SceneRenderer(VulkanContext vulkanContext, EngineContext engineContext) {
        clrValueColor = VkClearValue.calloc()
                .color(c -> c.float32(0, 0.2f).float32(1, 0.2f).float32(2, 0.8f).float32(3, 0.0f));
        clrValueDepth = VkClearValue.calloc().color(c -> c.float32(0, 1.0f));
        attColor = createColorAttachment(vulkanContext);
        attDepth = createDepthAttachment(vulkanContext);
        attInfoColor = createColorAttachmentInfo(attColor, clrValueColor);
        attInfoDepth = createDepthAttachmentInfo(attDepth, clrValueDepth);
        renderInfo = createRenderInfo(attColor, attInfoColor, attInfoDepth);

        ShaderModule[] shaderModules = createShaderModules(vulkanContext);

        pushConstBuff = MemoryUtil.memAlloc(PUSH_CONSTANTS_SIZE);

        descLayoutVtxUniform = new DescSetLayout(vulkanContext,
                new DescSetLayout.LayoutInfo(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 0, 1, VK_SHADER_STAGE_VERTEX_BIT));
        buffProjMatrix = VulkanUtils.createHostVisibleBuff(vulkanContext, VulkanUtils.MAT4X4_SIZE,
                VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, DESC_ID_PRJ, descLayoutVtxUniform);
        VulkanUtils.copyMatrixToBuffer(vulkanContext, buffProjMatrix,
                engineContext.scene().getProjection().getProjectionMatrix(), 0);

        buffViewMatrices = VulkanUtils.createHostVisibleBuffs(vulkanContext, VulkanUtils.MAT4X4_SIZE,
                VulkanUtils.MAX_IN_FLIGHT, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, DESC_ID_VIEW, descLayoutVtxUniform);

        descLayoutFrgStorage = new DescSetLayout(vulkanContext,
                new DescSetLayout.LayoutInfo(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, 0, 1, VK_SHADER_STAGE_FRAGMENT_BIT));

        var textureSamplerInfo = new TextureSamplerInfo(VK_SAMPLER_ADDRESS_MODE_REPEAT,
                VK_BORDER_COLOR_INT_OPAQUE_BLACK, 1, true);
        textureSampler = new TextureSampler(vulkanContext, textureSamplerInfo);
        descLayoutTexture = new DescSetLayout(vulkanContext, new DescSetLayout.LayoutInfo(
                VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 0, TextureCache.MAX_TEXTURES, VK_SHADER_STAGE_FRAGMENT_BIT));

        pipeline = createPipeline(vulkanContext, shaderModules, new DescSetLayout[] { descLayoutVtxUniform,
                descLayoutVtxUniform, descLayoutFrgStorage, descLayoutTexture });
        Arrays.asList(shaderModules).forEach(s -> s.cleanup(vulkanContext));
    }

    private static Attachment createColorAttachment(VulkanContext vulkanContext) {
        SwapChain swapChain = vulkanContext.getSwapChain();
        VkExtent2D swapChainExtent = swapChain.getSwapChainExtent();
        return new Attachment(vulkanContext, swapChainExtent.width(), swapChainExtent.height(), COLOR_FORMAT,
                VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
    }

    private static Attachment createDepthAttachment(VulkanContext vulkanContext) {
        SwapChain swapChain = vulkanContext.getSwapChain();
        VkExtent2D swapChainExtent = swapChain.getSwapChainExtent();
        return new Attachment(vulkanContext, swapChainExtent.width(), swapChainExtent.height(), DEPTH_FORMAT,
                VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT);
    }

    private static VkRenderingAttachmentInfo.Buffer createColorAttachmentInfo(Attachment attachment,
            VkClearValue clearValue) {
        return VkRenderingAttachmentInfo.calloc(1).sType$Default().imageView(attachment.getImageView().getVkImageView())
                .imageLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL).loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                .storeOp(VK_ATTACHMENT_STORE_OP_STORE).clearValue(clearValue);
    }

    private static VkRenderingAttachmentInfo createDepthAttachmentInfo(Attachment depthAttachment,
            VkClearValue clearValue) {
        return VkRenderingAttachmentInfo.calloc().sType$Default()
                .imageView(depthAttachment.getImageView().getVkImageView())
                .imageLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL).loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                .storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE).clearValue(clearValue);
    }

    private static VkRenderingInfo createRenderInfo(Attachment colorAttachment,
            VkRenderingAttachmentInfo.Buffer colorAttachmentInfo, VkRenderingAttachmentInfo depthAttachmentInfo) {
        VkRenderingInfo result;
        try (var stack = MemoryStack.stackPush()) {
            VkExtent2D extent = VkExtent2D.calloc(stack);
            extent.width(colorAttachment.getImage().getWidth());
            extent.height(colorAttachment.getImage().getHeight());
            var renderArea = VkRect2D.calloc(stack).extent(extent);

            result = VkRenderingInfo.calloc().sType$Default().renderArea(renderArea).layerCount(1)
                    .pColorAttachments(colorAttachmentInfo).pDepthAttachment(depthAttachmentInfo);
        }
        return result;
    }

    private static Pipeline createPipeline(VulkanContext vulkanContext, ShaderModule[] shaderModules,
            DescSetLayout[] descSetLayouts) {
        var vtxBuffStruct = new VertexBufferStruct();
        var buildInfo = new PipelineBuildInfo(shaderModules, vtxBuffStruct.getVi(), COLOR_FORMAT)
                .setDepthFormat(
                        DEPTH_FORMAT)
                .setPushConstRanges(new PushConstRange[] {
                        new PushConstRange(VK_SHADER_STAGE_VERTEX_BIT, 0, VulkanUtils.MAT4X4_SIZE), new PushConstRange(
                                VK_SHADER_STAGE_FRAGMENT_BIT, VulkanUtils.MAT4X4_SIZE, VulkanUtils.INT_SIZE), })
                .setDescSetLayouts(descSetLayouts).setUseBlend(true);
        var pipeline = new Pipeline(vulkanContext, buildInfo);
        vtxBuffStruct.cleanup();
        return pipeline;
    }

    private static ShaderModule[] createShaderModules(VulkanContext context) {
        if (EngineConfig.getInstance().isShaderRecompilation()) {
            ShaderCompiler.compileShaderIfChanged(VERTEX_SHADER_FILE_GLSL, Shaderc.shaderc_glsl_vertex_shader);
            ShaderCompiler.compileShaderIfChanged(FRAGMENT_SHADER_FILE_GLSL, Shaderc.shaderc_glsl_fragment_shader);
        }
        return new ShaderModule[] { new ShaderModule(context, VK_SHADER_STAGE_VERTEX_BIT, VERTEX_SHADER_FILE_SPV, null),
                new ShaderModule(context, VK_SHADER_STAGE_FRAGMENT_BIT, FRAGMENT_SHADER_FILE_SPV, null), };
    }

    public Attachment getAttColor() {
        return attColor;
    }

    public void cleanup(VulkanContext context) {
        pipeline.cleanup(context);
        Arrays.asList(buffViewMatrices).forEach(b -> b.cleanup(context));
        buffProjMatrix.cleanup(context);
        descLayoutVtxUniform.cleanup(context);
        descLayoutFrgStorage.cleanup(context);
        descLayoutTexture.cleanup(context);
        textureSampler.cleanup(context);
        renderInfo.free();
        attInfoDepth.free();
        attInfoColor.free();
        attColor.cleanup(context);
        attDepth.cleanup(context);
        MemoryUtil.memFree(pushConstBuff);
        clrValueDepth.free();
        clrValueColor.free();
    }

    public void loadMaterials(VulkanContext vulkanContext, MaterialsCache materialsCache, TextureCache textureCache) {
        DescAllocator descAllocator = vulkanContext.getDescAllocator();
        Device device = vulkanContext.getDevice();
        DescSet descSet = descAllocator.addDescSet(device, DESC_ID_MAT, descLayoutFrgStorage);
        DescSetLayout.LayoutInfo layoutInfo = descLayoutFrgStorage.getLayoutInfo();
        var buffer = materialsCache.getMaterialsBuffer();
        descSet.setBuffer(device, buffer, buffer.getRequestedSize(), layoutInfo.binding(), layoutInfo.descType());

        List<ImageView> imageViews = textureCache.getAsList().stream().map(Texture::getImageView).toList();
        descSet = vulkanContext.getDescAllocator().addDescSet(device, DESC_ID_TEXT, descLayoutTexture);
        descSet.setImagesArr(device, imageViews, textureSampler, 0);
    }

    public void render(EngineContext engineContext, VulkanContext vulkanContext, CommandBuffer cmdBuffer,
            ModelsCache modelsCache, MaterialsCache materialsCache, int currentFrame) {
        try (var stack = MemoryStack.stackPush()) {
            VkCommandBuffer cmdHandle = cmdBuffer.getVkCommandBuffer();

            VulkanUtils.imageBarrier(stack, cmdHandle, attColor.getImage().getVkImage(), VK_IMAGE_LAYOUT_UNDEFINED,
                    VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT,
                    VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT, VK_ACCESS_2_NONE,
                    VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT, VK_IMAGE_ASPECT_COLOR_BIT);
            VulkanUtils.imageBarrier(stack, cmdHandle, attDepth.getImage().getVkImage(), VK_IMAGE_LAYOUT_UNDEFINED,
                    VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL,
                    VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT | VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT,
                    VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT | VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT,
                    VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT,
                    VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT,
                    VK_IMAGE_ASPECT_DEPTH_BIT);

            vkCmdBeginRendering(cmdHandle, renderInfo);

            vkCmdBindPipeline(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.getVkPipeline());

            Image colorImage = attColor.getImage();
            int width = colorImage.getWidth();
            int height = colorImage.getHeight();
            var viewport = VkViewport.calloc(1, stack).x(0).y(height).height(-height).width(width).minDepth(0.0f)
                    .maxDepth(1.0f);
            vkCmdSetViewport(cmdHandle, 0, viewport);

            var scissor = VkRect2D.calloc(1, stack).extent(it -> it.width(width).height(height))
                    .offset(it -> it.x(0).y(0));
            vkCmdSetScissor(cmdHandle, 0, scissor);

            VulkanUtils.copyMatrixToBuffer(vulkanContext, buffViewMatrices[currentFrame],
                    engineContext.scene().getCamera().getViewMatrix(), 0);
            DescAllocator descAllocator = vulkanContext.getDescAllocator();
            LongBuffer descriptorSets = stack.mallocLong(4)
                    .put(0, descAllocator.getDescSet(DESC_ID_PRJ).getVkDescriptorSet())
                    .put(1, descAllocator.getDescSet(DESC_ID_VIEW, currentFrame).getVkDescriptorSet())
                    .put(2, descAllocator.getDescSet(DESC_ID_MAT).getVkDescriptorSet())
                    .put(3, descAllocator.getDescSet(DESC_ID_TEXT).getVkDescriptorSet());
            vkCmdBindDescriptorSets(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.getVkPipelineLayout(), 0,
                    descriptorSets, null);

            renderEntities(engineContext, cmdHandle, modelsCache, materialsCache, false);
            renderEntities(engineContext, cmdHandle, modelsCache, materialsCache, true);

            vkCmdEndRendering(cmdHandle);
        }
    }

    private void renderEntities(EngineContext engineContext, VkCommandBuffer cmdHandle, ModelsCache modelsCache,
            MaterialsCache materialsCache, boolean transparent) {
        try (var stack = MemoryStack.stackPush()) {
            LongBuffer vertexBuffer = stack.mallocLong(1);
            LongBuffer offsets = stack.mallocLong(1).put(0, 0L);

            Scene scene = engineContext.scene();
            List<Entity> entities = scene.getEntities();
            int numEntities = entities.size();
            for (int i = 0; i < numEntities; i++) {
                var entity = entities.get(i);
                VulkanModel model = modelsCache.getModel(entity.getModelId());
                if (model == null) {
                    Logger.warn("Entity [{}] does not have model", i);
                    continue;
                }
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
                    if (vulkanMaterial.isTransparent() == transparent) {
                        setPushConstants(cmdHandle, entity.getModelMatrix(), materialIdx);
                        vertexBuffer.put(0, vulkanMesh.verticesBuffer().getBuffer());
                        vkCmdBindVertexBuffers(cmdHandle, 0, vertexBuffer, offsets);
                        vkCmdBindIndexBuffer(cmdHandle, vulkanMesh.indicesBuffer().getBuffer(), 0,
                                VK_INDEX_TYPE_UINT32);
                        vkCmdDrawIndexed(cmdHandle, vulkanMesh.numIndices(), 1, 0, 0, 0);
                    }
                }
            }
        }
    }

    public void resize(EngineContext engineContext, VulkanContext vulkanContext) {
        renderInfo.free();
        attInfoColor.free();
        attInfoDepth.free();
        attColor.cleanup(vulkanContext);
        attDepth.cleanup(vulkanContext);

        attColor = createColorAttachment(vulkanContext);
        attDepth = createDepthAttachment(vulkanContext);
        attInfoColor = createColorAttachmentInfo(attColor, clrValueColor);
        attInfoDepth = createDepthAttachmentInfo(attDepth, clrValueDepth);
        renderInfo = createRenderInfo(attColor, attInfoColor, attInfoDepth);

        VulkanUtils.copyMatrixToBuffer(vulkanContext, buffProjMatrix,
                engineContext.scene().getProjection().getProjectionMatrix(), 0);
    }

    private void setPushConstants(VkCommandBuffer cmdHandle, Matrix4f modelMatrix, int materialIdx) {
        modelMatrix.get(0, pushConstBuff);
        pushConstBuff.putInt(VulkanUtils.MAT4X4_SIZE, materialIdx);
        vkCmdPushConstants(cmdHandle, pipeline.getVkPipelineLayout(), VK_SHADER_STAGE_VERTEX_BIT, 0,
                pushConstBuff.slice(0, VulkanUtils.MAT4X4_SIZE));
        vkCmdPushConstants(cmdHandle, pipeline.getVkPipelineLayout(), VK_SHADER_STAGE_FRAGMENT_BIT,
                VulkanUtils.MAT4X4_SIZE, pushConstBuff.slice(VulkanUtils.MAT4X4_SIZE, VulkanUtils.INT_SIZE));
    }
}
