package com.deepwelldevelopment.spacequest.engine.graph.vk;

import static com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanUtils.vkCheck;
import static org.lwjgl.vulkan.VK10.vkCreateShaderModule;
import static org.lwjgl.vulkan.VK10.vkDestroyShaderModule;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.file.Files;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.tinylog.Logger;

public class ShaderModule {

    private final long handle;
    private final int shaderStage;

    public ShaderModule(VulkanContext context, int shaderStage, String shaderSpvFile) {
        try {
            byte[] moduleContents = Files.readAllBytes(new File(shaderSpvFile).toPath());
            handle = createShaderModule(context, moduleContents);
            this.shaderStage = shaderStage;
        } catch (IOException excp) {
            Logger.error("Error reading shader file", excp);
            throw new RuntimeException(excp);
        }
    }

    private static long createShaderModule(VulkanContext context, byte[] code) {
        try (var stack = MemoryStack.stackPush()) {
            ByteBuffer pCode = stack.malloc(code.length).put(0, code);

            var moduleCreateInfo = VkShaderModuleCreateInfo.calloc(stack)
                    .sType$Default()
                    .pCode(pCode);

            LongBuffer lp = stack.mallocLong(1);
            vkCheck(vkCreateShaderModule(context.getDevice().getVkDevice(), moduleCreateInfo, null, lp),
                    "Failed to create shader module");

            return lp.get(0);
        }
    }

    public void cleanup(VulkanContext context) {
        vkDestroyShaderModule(context.getDevice().getVkDevice(), handle, null);
    }

    public long getHandle() {
        return handle;
    }

    public int getShaderStage() {
        return shaderStage;
    }
}
