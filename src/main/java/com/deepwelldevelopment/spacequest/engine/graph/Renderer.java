package com.deepwelldevelopment.spacequest.engine.graph;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SRGB;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBufferSubmitInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkSemaphoreSubmitInfo;
import org.tinylog.Logger;

import com.deepwelldevelopment.spacequest.engine.EngineContext;
import com.deepwelldevelopment.spacequest.engine.GuiTexture;
import com.deepwelldevelopment.spacequest.engine.graph.gui.GuiRenderer;
import com.deepwelldevelopment.spacequest.engine.graph.post.PostRenderer;
import com.deepwelldevelopment.spacequest.engine.graph.scene.SceneRenderer;
import com.deepwelldevelopment.spacequest.engine.graph.swap.SwapChainRenderer;
import com.deepwelldevelopment.spacequest.engine.graph.vk.CommandBuffer;
import com.deepwelldevelopment.spacequest.engine.graph.vk.CommandPool;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Fence;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Queue;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Semaphore;
import com.deepwelldevelopment.spacequest.engine.graph.vk.SwapChain;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanContext;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanUtils;
import com.deepwelldevelopment.spacequest.engine.model.MaterialData;
import com.deepwelldevelopment.spacequest.engine.model.ModelData;
import com.deepwelldevelopment.spacequest.engine.model.VoxelMaterialManager;
import com.deepwelldevelopment.spacequest.engine.model.VoxelModelFactory;
import com.deepwelldevelopment.spacequest.engine.scene.Scene;
import com.deepwelldevelopment.spacequest.engine.window.Window;
import com.deepwelldevelopment.world.World;
import com.deepwelldevelopment.world.chunk.Chunk;

public class Renderer {

    private final CommandBuffer[] commandBuffers;
    private final CommandPool[] commandPools;
    private final Fence[] fences;
    private final Queue.GraphicsQueue graphicsQueue;
    private final Semaphore[] presentationCompleteSemaphores;
    private final MaterialsCache materialsCache;
    private final ModelsCache modelsCache;
    private final Queue.PresentQueue presentQueue;
    private final Semaphore[] renderCompleteSemaphores;
    private final SceneRenderer sceneRenderer;
    private final PostRenderer postRenderer;
    private final GuiRenderer guiRenderer;
    private final SwapChainRenderer swapChainRenderer;
    private final TextureCache textureCache;
    private final VulkanContext vulkanContext;
    private final List<String> modelsToRemove = new ArrayList<>();
    private int currentFrame;
    private boolean resize;

    public Renderer(EngineContext engineContext) {
        vulkanContext = new VulkanContext(engineContext.window());
        currentFrame = 0;

        graphicsQueue = new Queue.GraphicsQueue(vulkanContext, 0);
        presentQueue = new Queue.PresentQueue(vulkanContext, 0);

        commandPools = new CommandPool[VulkanUtils.MAX_IN_FLIGHT];
        commandBuffers = new CommandBuffer[VulkanUtils.MAX_IN_FLIGHT];
        fences = new Fence[VulkanUtils.MAX_IN_FLIGHT];
        presentationCompleteSemaphores = new Semaphore[VulkanUtils.MAX_IN_FLIGHT];
        int numSwapChainImages = vulkanContext.getSwapChain().getNumImages();
        renderCompleteSemaphores = new Semaphore[numSwapChainImages];
        for (int i = 0; i < VulkanUtils.MAX_IN_FLIGHT; i++) {
            commandPools[i] = new CommandPool(vulkanContext, graphicsQueue.getQueueFamilyIndex(), false);
            commandBuffers[i] = new CommandBuffer(vulkanContext, commandPools[i], true, true);
            fences[i] = new Fence(vulkanContext, true);
            presentationCompleteSemaphores[i] = new Semaphore(vulkanContext);
        }
        for (int i = 0; i < numSwapChainImages; i++) {
            renderCompleteSemaphores[i] = new Semaphore(vulkanContext);
        }
        resize = false;
        sceneRenderer = new SceneRenderer(vulkanContext, engineContext);
        postRenderer = new PostRenderer(vulkanContext, sceneRenderer.getAttColor());
        guiRenderer = new GuiRenderer(engineContext, vulkanContext, graphicsQueue, postRenderer.getAttachment());
        swapChainRenderer = new SwapChainRenderer(vulkanContext, postRenderer.getAttachment());
        modelsCache = new ModelsCache();
        textureCache = new TextureCache();
        materialsCache = new MaterialsCache();
    }

    public void cleanup() {
        vulkanContext.getDevice().waitIdle();

        sceneRenderer.cleanup(vulkanContext);
        guiRenderer.cleanup(vulkanContext);

        modelsCache.cleanup(vulkanContext);
        textureCache.cleanup(vulkanContext);
        materialsCache.cleanup(vulkanContext);

        Arrays.asList(renderCompleteSemaphores).forEach(i -> i.cleanup(vulkanContext));
        Arrays.asList(presentationCompleteSemaphores).forEach(i -> i.cleanup(vulkanContext));
        Arrays.asList(fences).forEach(i -> i.cleanup(vulkanContext));
        for (int i = 0; i < commandPools.length; i++) {
            commandBuffers[i].cleanup(vulkanContext, commandPools[i]);
            commandPools[i].cleanup(vulkanContext);
        }

        vulkanContext.cleanup();
    }

    public void init(EngineContext engineContext, World world) {
        Scene scene = engineContext.scene();
        List<ModelData> models = new ArrayList<>();
        // Create voxel models from world chunks
        List<VoxelModelFactory.VoxelModelData> voxelModels = new ArrayList<>();

        for (Chunk chunk : world.getChunks()) {
            if (chunk != null) {
                var chunkMesh = chunk.getChunkMesh();
                if (chunkMesh != null) {
                    voxelModels.addAll(chunkMesh.getVoxelModels());
                }
            }
        }

        // Add all voxel models to scene
        VoxelModelFactory.addVoxelModelsToScene(scene, models, voxelModels);

        modelsCache.loadModels(vulkanContext, models, commandPools[0], graphicsQueue);

        List<MaterialData> materials = new ArrayList<>();
        materials.addAll(VoxelMaterialManager.getAllMaterials());
        materialsCache.loadMaterials(vulkanContext, materials, textureCache, commandPools[0], graphicsQueue);

        List<GuiTexture> guiTextures = new ArrayList<>(List.of(new GuiTexture("resources/textures/vulkan.png")));
        if (guiTextures != null) {
            guiTextures.forEach(e -> textureCache.addTexture(vulkanContext, e.texturePath(), e.texturePath(),
                    VK_FORMAT_R8G8B8A8_SRGB));
        }

        Logger.debug("Transitioning textures");
        textureCache.transitionTexts(vulkanContext, commandPools[0], graphicsQueue);
        Logger.debug("Textures transitioned");

        sceneRenderer.loadMaterials(vulkanContext, materialsCache, textureCache);
        guiRenderer.loadTextures(vulkanContext, guiTextures, textureCache);
    }

    private void recordingStart(CommandPool cmdPool, CommandBuffer cmdBuffer) {
        cmdPool.reset(vulkanContext);
        cmdBuffer.beginRecording();
    }

    private void recordingStop(CommandBuffer cmdBuffer) {
        cmdBuffer.endRecording();
    }

    public void render(EngineContext engCtx) {
        SwapChain swapChain = vulkanContext.getSwapChain();

        waitForFence(currentFrame);

        var cmdPool = commandPools[currentFrame];
        var cmdBuffer = commandBuffers[currentFrame];

        recordingStart(cmdPool, cmdBuffer);

        sceneRenderer.render(engCtx, vulkanContext, cmdBuffer, modelsCache, materialsCache, currentFrame);
        postRenderer.render(vulkanContext, cmdBuffer, sceneRenderer.getAttColor());
        guiRenderer.render(vulkanContext, cmdBuffer, currentFrame, postRenderer.getAttachment());

        int imageIndex;
        if (resize || (imageIndex = swapChain.acquireNextImage(vulkanContext.getDevice(),
                presentationCompleteSemaphores[currentFrame])) < 0) {
            resize(engCtx);
            return;
        }

        swapChainRenderer.render(vulkanContext, cmdBuffer, postRenderer.getAttachment(), imageIndex);

        recordingStop(cmdBuffer);

        submit(cmdBuffer, currentFrame, imageIndex);

        resize = swapChain.presentImage(presentQueue, renderCompleteSemaphores[imageIndex], imageIndex);

        // Clean up models if we have accumulated too many
        cleanupDeferredModels();

        currentFrame = (currentFrame + 1) % VulkanUtils.MAX_IN_FLIGHT;
    }

    public void addModel(ModelData modelData) {
        modelsCache.addModel(vulkanContext, modelData, commandPools[0], graphicsQueue);
    }

    public void updateModel(ModelData modelData) {
        modelsCache.updateModel(vulkanContext, modelData, commandPools[0], graphicsQueue);
    }

    public void removeModel(String modelId) {
        // Defer removal until it's safe to destroy resources
        modelsToRemove.add(modelId);
    }

    public boolean hasModel(String modelId) {
        return modelsCache.hasModel(modelId);
    }

    public void triggerCleanup() {
        // Don't cleanup immediately - defer to prevent flickering
        // Models will be cleaned up less frequently to avoid blocking
    }

    private void cleanupDeferredModels() {
        if (modelsToRemove.size() > 50) { // Only cleanup when we have many models to remove
            vulkanContext.getDevice().waitIdle();
            for (String modelId : modelsToRemove) {
                modelsCache.removeModel(vulkanContext, modelId);
            }
            modelsToRemove.clear();
        }
    }

    private void resize(EngineContext engineContext) {
        Window window = engineContext.window();
        if (window.getWidth() == 0 && window.getHeight() == 0) {
            return;
        }
        resize = false;
        vulkanContext.getDevice().waitIdle();

        vulkanContext.resize(window);

        Arrays.asList(renderCompleteSemaphores).forEach(i -> i.cleanup(vulkanContext));
        Arrays.asList(presentationCompleteSemaphores).forEach(i -> i.cleanup(vulkanContext));
        for (int i = 0; i < VulkanUtils.MAX_IN_FLIGHT; i++) {
            presentationCompleteSemaphores[i] = new Semaphore(vulkanContext);
        }
        for (int i = 0; i < vulkanContext.getSwapChain().getNumImages(); i++) {
            renderCompleteSemaphores[i] = new Semaphore(vulkanContext);
        }

        VkExtent2D extent = vulkanContext.getSwapChain().getSwapChainExtent();
        engineContext.scene().getProjection().resize(extent.width(), extent.height());
        sceneRenderer.resize(engineContext, vulkanContext);
        postRenderer.resize(vulkanContext, sceneRenderer.getAttColor());
        guiRenderer.resize(vulkanContext, postRenderer.getAttachment());
        swapChainRenderer.resize(vulkanContext, postRenderer.getAttachment());
    }

    private void submit(CommandBuffer cmdBuff, int currentFrame, int imageIndex) {
        try (var stack = MemoryStack.stackPush()) {
            var fence = fences[currentFrame];
            fence.reset(vulkanContext);
            var cmds = VkCommandBufferSubmitInfo.calloc(1, stack).sType$Default()
                    .commandBuffer(cmdBuff.getVkCommandBuffer());
            VkSemaphoreSubmitInfo.Buffer waitSemaphores = VkSemaphoreSubmitInfo.calloc(1, stack).sType$Default()
                    .stageMask(VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT)
                    .semaphore(presentationCompleteSemaphores[currentFrame].getVkSemaphore());
            VkSemaphoreSubmitInfo.Buffer signalSemaphores = VkSemaphoreSubmitInfo.calloc(1, stack).sType$Default()
                    .stageMask(VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT)
                    .semaphore(renderCompleteSemaphores[imageIndex].getVkSemaphore());
            graphicsQueue.submit(cmds, waitSemaphores, signalSemaphores, fence);
        }
    }

    private void waitForFence(int currentFrame) {
        var fence = fences[currentFrame];
        fence.fenceWait(vulkanContext);
    }
}
