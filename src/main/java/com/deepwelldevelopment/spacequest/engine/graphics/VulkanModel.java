package com.deepwelldevelopment.spacequest.engine.graphics;

import java.util.ArrayList;
import java.util.List;

import com.deepwelldevelopment.spacequest.engine.graphics.vk.VulkanContext;

public class VulkanModel {

    private final String id;
    private final List<VulkanMesh> meshes;

    public VulkanModel(String id) {
        this.id = id;
        meshes = new ArrayList<>();
    }

    public void cleanup(VulkanContext context) {
        meshes.forEach(mesh -> mesh.cleanup(context));
    }

    public String getId() {
        return id;
    }

    public List<VulkanMesh> getMeshes() {
        return meshes;
    }
}
