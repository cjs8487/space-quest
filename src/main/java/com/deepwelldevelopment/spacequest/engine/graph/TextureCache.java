package com.deepwelldevelopment.spacequest.engine.graph;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SRGB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.tinylog.Logger;

import com.deepwelldevelopment.spacequest.engine.EngineConfig;
import com.deepwelldevelopment.spacequest.engine.graph.vk.CommandBuffer;
import com.deepwelldevelopment.spacequest.engine.graph.vk.CommandPool;
import com.deepwelldevelopment.spacequest.engine.graph.vk.ImageSrc;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Queue;
import com.deepwelldevelopment.spacequest.engine.graph.vk.Texture;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanContext;

public class TextureCache {
    public static final int MAX_TEXTURES = 80;
    private final IndexedLinkedHashMap<String, Texture> textureMap;

    public TextureCache() {
        textureMap = new IndexedLinkedHashMap<>();
    }

    public Texture addTexture(VulkanContext vulkanContext, String id, ImageSrc srcImage, int format) {
        if (textureMap.size() > MAX_TEXTURES) {
            throw new IllegalArgumentException("Texture cache is full");
        }
        Texture texture = textureMap.get(id);
        if (texture == null) {
            texture = new Texture(vulkanContext, id, srcImage, format);
            textureMap.put(id, texture);
        }
        return texture;
    }

    public Texture addTexture(VulkanContext vulkanContext, String id, String texturePath, int format) {
        ImageSrc srcImage = null;
        Texture result = null;
        try {
            srcImage = GraphicsUtils.loadImage(texturePath);
            result = addTexture(vulkanContext, id, srcImage, format);
        } catch (IOException excp) {
            Logger.error("Could not load texture [{}], {}", texturePath, excp);
        } finally {
            if (srcImage != null) {
                GraphicsUtils.cleanImageData(srcImage);
            }
        }
        return result;
    }

    public void cleanup(VulkanContext vulkanContext) {
        textureMap.forEach((k, t) -> t.cleanup(vulkanContext));
        textureMap.clear();
    }

    public List<Texture> getAsList() {
        return new ArrayList<>(textureMap.values());
    }

    public int getPosition(String id) {
        int result = -1;
        if (id != null) {
            result = textureMap.getIndexOf(id);
        }
        return result;
    }

    public Texture getTexture(String texturePath) {
        return textureMap.get(texturePath.trim());
    }

    public void transitionTexts(VulkanContext vulkanContext, CommandPool cmdPool, Queue queue) {
        Logger.debug("Recording texture transitions");
        int numTextures = textureMap.size();
        if (numTextures < MAX_TEXTURES) {
            int numPaddingTexts = MAX_TEXTURES - numTextures;
            String defaultTexturePath = EngineConfig.getInstance().getDefaultTexturePath();
            for (int i = 0; i < numPaddingTexts; i++) {
                addTexture(vulkanContext, UUID.randomUUID().toString(), defaultTexturePath, VK_FORMAT_R8G8B8A8_SRGB);
            }
        }
        var cmdBuf = new CommandBuffer(vulkanContext, cmdPool, true, true);
        cmdBuf.beginRecording();
        textureMap.forEach((k, v) -> v.recordTextureTransition(cmdBuf));
        cmdBuf.endRecording();
        cmdBuf.submitAndWait(vulkanContext, queue);
        cmdBuf.cleanup(vulkanContext, cmdPool);
        textureMap.forEach((k, v) -> v.cleanupStgBuffer(vulkanContext));
        Logger.debug("Recorded texture transitions");
    }
}
