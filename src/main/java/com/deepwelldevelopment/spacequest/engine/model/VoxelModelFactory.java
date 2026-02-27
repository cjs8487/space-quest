package com.deepwelldevelopment.spacequest.engine.model;

import java.util.List;

import org.joml.Vector3f;

import com.deepwelldevelopment.spacequest.engine.model.ProgrammaticModel.ProgrammaticMesh;
import com.deepwelldevelopment.spacequest.engine.scene.Entity;
import com.deepwelldevelopment.spacequest.engine.scene.Scene;

/**
 * Factory class for creating voxel-based models and entities.
 * Provides a clean separation between voxel and non-voxel model creation.
 */
public class VoxelModelFactory {

    public static class VoxelModelData {
        public final ProgrammaticModel model;
        public final ModelData modelData;
        public final Entity entity;

        public VoxelModelData(ProgrammaticModel model, ModelData modelData, Entity entity) {
            this.model = model;
            this.modelData = modelData;
            this.entity = entity;
        }
    }

    /**
     * Creates a standard voxel block (1x1x1 cube)
     */
    public static VoxelModelData createBlock(String id, String materialId, Vector3f position) {
        return createBlock(id, materialId, position, 1.0f);
    }

    /**
     * Creates a voxel block with custom size
     */
    public static VoxelModelData createBlock(String id, String materialId, Vector3f position, float size) {
        ProgrammaticModel model = MeshBuilder.createCube(id, materialId, size);
        ModelData modelData = model.toModelData();
        Entity entity = new Entity(id + "_entity", modelData.id(), position);

        return new VoxelModelData(model, modelData, entity);
    }

    /**
     * Creates a custom voxel structure from a 3D array
     */
    public static VoxelModelData createFromVoxelData(String id, VoxelType[][][] voxelData,
            Vector3f position) {
        if (voxelData == null || voxelData.length == 0) {
            throw new IllegalArgumentException("Voxel data cannot be null or empty");
        }

        ProgrammaticModel model = new ProgrammaticModel(id);
        int meshIndex = 0;

        for (int y = 0; y < voxelData.length; y++) {
            for (int z = 0; z < voxelData[y].length; z++) {
                for (int x = 0; x < voxelData[y][z].length; x++) {
                    VoxelType voxelType = voxelData[y][z][x];
                    if (voxelType != null && voxelType != VoxelType.AIR) {
                        String meshId = id + "_mesh_" + meshIndex++;
                        ProgrammaticMesh mesh = model.addMesh(meshId, voxelType.getMaterialId());
                        createCubeMesh(mesh, x, y, z, 1.0f);
                    }
                }
            }
        }

        ModelData modelData = model.toModelData();
        Entity entity = new Entity(id + "_entity", modelData.id(), position);

        return new VoxelModelData(model, modelData, entity);
    }

    /**
     * Helper method to create a cube mesh at a specific position
     */
    private static void createCubeMesh(ProgrammaticMesh mesh, float x, float y, float z, float size) {
        float halfSize = size / 2.0f;
        int startVertex = mesh.vertices.size() / 3;

        // Front face
        mesh.addVertex(x - halfSize, y - halfSize, z + halfSize, 0.0f, 1.0f)
                .addVertex(x + halfSize, y - halfSize, z + halfSize, 1.0f, 1.0f)
                .addVertex(x + halfSize, y + halfSize, z + halfSize, 1.0f, 0.0f)
                .addVertex(x - halfSize, y + halfSize, z + halfSize, 0.0f, 0.0f);

        // Back face
        mesh.addVertex(x + halfSize, y - halfSize, z - halfSize, 0.0f, 1.0f)
                .addVertex(x - halfSize, y - halfSize, z - halfSize, 1.0f, 1.0f)
                .addVertex(x - halfSize, y + halfSize, z - halfSize, 1.0f, 0.0f)
                .addVertex(x + halfSize, y + halfSize, z - halfSize, 0.0f, 0.0f);

        // Top face
        mesh.addVertex(x - halfSize, y + halfSize, z + halfSize, 0.0f, 0.0f)
                .addVertex(x + halfSize, y + halfSize, z + halfSize, 1.0f, 0.0f)
                .addVertex(x + halfSize, y + halfSize, z - halfSize, 1.0f, 1.0f)
                .addVertex(x - halfSize, y + halfSize, z - halfSize, 0.0f, 1.0f);

        // Bottom face
        mesh.addVertex(x - halfSize, y - halfSize, z - halfSize, 0.0f, 1.0f)
                .addVertex(x + halfSize, y - halfSize, z - halfSize, 1.0f, 1.0f)
                .addVertex(x + halfSize, y - halfSize, z + halfSize, 1.0f, 0.0f)
                .addVertex(x - halfSize, y - halfSize, z + halfSize, 0.0f, 0.0f);

        // Right face
        mesh.addVertex(x + halfSize, y - halfSize, z + halfSize, 0.0f, 1.0f)
                .addVertex(x + halfSize, y - halfSize, z - halfSize, 1.0f, 1.0f)
                .addVertex(x + halfSize, y + halfSize, z - halfSize, 1.0f, 0.0f)
                .addVertex(x + halfSize, y + halfSize, z + halfSize, 0.0f, 0.0f);

        // Left face
        mesh.addVertex(x - halfSize, y - halfSize, z - halfSize, 0.0f, 1.0f)
                .addVertex(x - halfSize, y - halfSize, z + halfSize, 1.0f, 1.0f)
                .addVertex(x - halfSize, y + halfSize, z + halfSize, 1.0f, 0.0f)
                .addVertex(x - halfSize, y + halfSize, z - halfSize, 0.0f, 0.0f);

        // Add indices for each face
        mesh.addQuad(startVertex, startVertex + 1, startVertex + 2, startVertex + 3); // Front
        mesh.addQuad(startVertex + 4, startVertex + 5, startVertex + 6, startVertex + 7); // Back
        mesh.addQuad(startVertex + 8, startVertex + 9, startVertex + 10, startVertex + 11); // Top
        mesh.addQuad(startVertex + 12, startVertex + 13, startVertex + 14, startVertex + 15); // Bottom
        mesh.addQuad(startVertex + 16, startVertex + 17, startVertex + 18, startVertex + 19); // Right
        mesh.addQuad(startVertex + 20, startVertex + 21, startVertex + 22, startVertex + 23); // Left

        mesh.finalizeMesh();
    }

    /**
     * Utility method to add multiple voxel models to scene
     */
    public static void addVoxelModelsToScene(Scene scene, List<ModelData> models,
            List<VoxelModelData> voxelModels) {
        for (VoxelModelData voxelModel : voxelModels) {
            models.add(voxelModel.modelData);
            scene.addEntity(voxelModel.entity);
        }
    }
}
