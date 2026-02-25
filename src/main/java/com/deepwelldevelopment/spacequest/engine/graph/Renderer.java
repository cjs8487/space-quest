package com.deepwelldevelopment.spacequest.engine.graph;

import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBufferSubmitInfo;
import org.lwjgl.vulkan.VkSemaphoreSubmitInfo;
import org.tinylog.Logger;

import com.deepwelldevelopment.spacequest.engine.EngineContext;
import com.deepwelldevelopment.spacequest.engine.graph.scene.SceneRenderer;
import com.deepwelldevelopment.spacequest.engine.graph.vk.CommandBuffer;
import com.deepwelldevelopment.spacequest.engine.graph.vk.CommandPool;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Fence;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Queue;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Semaphore;
import com.deepwelldevelopment.spacequest.engine.graph.vk.SwapChain;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanContext;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanUtils;
import com.deepwelldevelopment.spacequest.engine.model.ModelData;

public class Renderer {

    private final CommandBuffer[] commandBuffers;
    private final CommandPool[] commandPools;
    private final Fence[] fences;
    private final Queue.GraphicsQueue graphicsQueue;
    private final Semaphore[] presentationCompleteSemaphores;
    private final Queue.PresentQueue presentQueue;
    private final Semaphore[] renderCompleteSemaphores;
    private final SceneRenderer sceneRenderer;
    private final VulkanContext vulkanContext;
    private final ModelsCache modelsCache;
    private int currentFrame;

    public Renderer(EngineContext context) {
        vulkanContext = new VulkanContext(context.window());
        currentFrame = 0;

        graphicsQueue = new Queue.GraphicsQueue(vulkanContext, 0);
        presentQueue = new Queue.PresentQueue(vulkanContext, 0);

        commandPools = new CommandPool[VulkanUtils.MAX_IN_FLIGHT];
        commandBuffers = new CommandBuffer[VulkanUtils.MAX_IN_FLIGHT];
        fences = new Fence[VulkanUtils.MAX_IN_FLIGHT];
        presentationCompleteSemaphores = new Semaphore[VulkanUtils.MAX_IN_FLIGHT];
        int numSwapchainImages = vulkanContext.getSwapChain().getNumImages();
        renderCompleteSemaphores = new Semaphore[numSwapchainImages];
        for (int i = 0; i < VulkanUtils.MAX_IN_FLIGHT; i++) {
            commandPools[i] = new CommandPool(vulkanContext, graphicsQueue.getQueueFamilyIndex(), false);
            commandBuffers[i] = new CommandBuffer(vulkanContext, commandPools[i], true, true);
            presentationCompleteSemaphores[i] = new Semaphore(vulkanContext);
            fences[i] = new Fence(vulkanContext, true);
        }
        for (int i = 0; i < numSwapchainImages; i++) {
            renderCompleteSemaphores[i] = new Semaphore(vulkanContext);
        }
        sceneRenderer = new SceneRenderer(vulkanContext);
        modelsCache = new ModelsCache();
    }

    public void cleanup() {
        vulkanContext.getDevice().waitIdle();

        sceneRenderer.cleanup(vulkanContext);

        Arrays.asList(renderCompleteSemaphores).forEach(i -> i.cleanup(vulkanContext));
        Arrays.asList(presentationCompleteSemaphores).forEach(i -> i.cleanup(vulkanContext));
        Arrays.asList(fences).forEach(i -> i.cleanup(vulkanContext));
        for (int i = 0; i < commandBuffers.length; i++) {
            var pool = commandPools[i];
            commandBuffers[i].cleanup(vulkanContext, pool);
            pool.cleanup(vulkanContext);
        }

        vulkanContext.cleanup();
    }

    public void init(List<ModelData> models) {
        Logger.debug("Loading {} model(s)", models.size());
        modelsCache.loadModels(vulkanContext, models, commandPools[0], graphicsQueue);
        Logger.debug("Loaded {} model(s)", models.size());
    }

    private void recordingStart(CommandPool pool, CommandBuffer buffer) {
        pool.reset(vulkanContext);
        buffer.beginRecording();
    }

    private void recordingStop(CommandBuffer buffer) {
        buffer.endRecording();
    }

    public void render(EngineContext context) {
        SwapChain swapChain = vulkanContext.getSwapChain();

        waitForFence(currentFrame);

        var pool = commandPools[currentFrame];
        var buffer = commandBuffers[currentFrame];

        recordingStart(pool, buffer);

        int imageIndex = swapChain.acquireNextImage(vulkanContext.getDevice(),
                presentationCompleteSemaphores[currentFrame]);
        if (imageIndex < 0) {
            return;
        }
        sceneRenderer.render(vulkanContext, buffer, modelsCache, imageIndex);

        recordingStop(buffer);

        submit(buffer, currentFrame, imageIndex);

        swapChain.presentImage(presentQueue, renderCompleteSemaphores[imageIndex], imageIndex);
        currentFrame = (currentFrame + 1) % VulkanUtils.MAX_IN_FLIGHT;
    }

    private void submit(CommandBuffer buffer, int frame, int imageIndex) {
        try (var stack = MemoryStack.stackPush()) {
            var fence = fences[frame];
            fence.reset(vulkanContext);
            var commands = VkCommandBufferSubmitInfo.calloc(1, stack)
                    .sType$Default()
                    .commandBuffer(buffer.getVkCommandBuffer());
            VkSemaphoreSubmitInfo.Buffer waitSemaphores = VkSemaphoreSubmitInfo.calloc(1, stack)
                    .sType$Default()
                    .stageMask(VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT)
                    .semaphore(presentationCompleteSemaphores[frame].getVkSemaphore());
            VkSemaphoreSubmitInfo.Buffer signalSemaphores = VkSemaphoreSubmitInfo.calloc(1, stack)
                    .sType$Default()
                    .stageMask(VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT)
                    .semaphore(renderCompleteSemaphores[imageIndex].getVkSemaphore());
            graphicsQueue.submit(commands, waitSemaphores, signalSemaphores, fence);
        }
    }

    private void waitForFence(int frame) {
        var fence = fences[frame];
        fence.fenceWait(vulkanContext);
    }
}
