package com.deepwelldevelopment.spacequest.engine.graph.post;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkSpecializationInfo;
import org.lwjgl.vulkan.VkSpecializationMapEntry;

import com.deepwelldevelopment.spacequest.engine.EngineConfig;
import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanUtils;

public class SpecConstants {
    private final ByteBuffer data;
    private final VkSpecializationMapEntry.Buffer specEntryMap;
    private final VkSpecializationInfo specInfo;

    public SpecConstants() {
        var engCfg = EngineConfig.getInstance();
        data = MemoryUtil.memAlloc(VulkanUtils.INT_SIZE);
        data.putInt(engCfg.isFxaa() ? 1 : 0);
        data.flip();

        specEntryMap = VkSpecializationMapEntry.calloc(1);
        specEntryMap.get(0).constantID(0).size(VulkanUtils.INT_SIZE).offset(0);

        specInfo = VkSpecializationInfo.calloc();
        specInfo.pData(data).pMapEntries(specEntryMap);
    }

    public void cleanup() {
        MemoryUtil.memFree(specEntryMap);
        specInfo.free();
        MemoryUtil.memFree(data);
    }

    public VkSpecializationInfo getSpecInfo() {
        return specInfo;
    }
}
