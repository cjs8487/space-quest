package com.deepwelldevelopment.spacequest.engine.model;

import java.util.List;

import org.joml.Vector3f;

import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.block.Blocks;
import com.deepwelldevelopment.spacequest.block.Block.Side;
import com.deepwelldevelopment.spacequest.engine.model.ProgrammaticModel.ProgrammaticMesh;
import com.deepwelldevelopment.spacequest.engine.scene.Entity;
import com.deepwelldevelopment.spacequest.engine.scene.Scene;
import com.deepwelldevelopment.world.World;
import com.deepwelldevelopment.world.chunk.Chunk;
import com.deepwelldevelopment.spacequest.engine.model.GreedyMesher.GreedyMeshResult;

/**
 * Factory class for creating voxel-based models and entities. Provides a clean
 * separation between voxel and non-voxel model creation.
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
     * Creates a custom voxel structure from a 3D array using greedy meshing for
     * optimization
     */
    public static VoxelModelData createFromBlocksGreedy(String id, Chunk chunk, Vector3f position) {
        GreedyMeshResult greedyResult = GreedyMesher.createGreedyMesh(id, chunk);

        // Check if the model has any meshes (faces) - if not, return null
        if (greedyResult.faceCount == 0 || greedyResult.vertexCount == 0) {
            return null;
        }

        ModelData modelData = greedyResult.model.toModelData();
        Entity entity = new Entity(id + "_entity", modelData.id(), position);

        return new VoxelModelData(greedyResult.model, modelData, entity);
    }

    /**
     * Creates a custom voxel structure from a 3D array (legacy method for
     * comparison)
     */
    public static VoxelModelData createFromBlocks(String id, Chunk chunk, Vector3f position) {
        ProgrammaticModel model = new ProgrammaticModel(id);
        int meshIndex = 0;

        for (int y = 0; y < World.CHUNK_SIZE; y++) {
            for (int z = 0; z < World.CHUNK_SIZE; z++) {
                for (int x = 0; x < World.CHUNK_SIZE; x++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (block != null && block != Blocks.AIR) {
                        boolean drawTop = block.shouldRenderSide(chunk, x, y, z, Side.TOP);
                        boolean drawBottom = block.shouldRenderSide(chunk, x, y, z, Side.BOTTOM);
                        boolean drawFront = block.shouldRenderSide(chunk, x, y, z, Side.FRONT);
                        boolean drawBack = block.shouldRenderSide(chunk, x, y, z, Side.BACK);
                        boolean drawLeft = block.shouldRenderSide(chunk, x, y, z, Side.LEFT);
                        boolean drawRight = block.shouldRenderSide(chunk, x, y, z, Side.RIGHT);

                        String meshId = id + "_mesh_" + meshIndex++;
                        ProgrammaticMesh mesh = model.addMesh(meshId, block.getMaterialName());
                        createCubeMesh(mesh, x, y, z, 1.0f, drawTop, drawBottom, drawFront, drawBack, drawLeft,
                                drawRight);
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
    private static void createCubeMesh(ProgrammaticMesh mesh, float x, float y, float z, float size, boolean drawTop,
            boolean drawBottom, boolean drawFront, boolean drawBack, boolean drawLeft, boolean drawRight) {
        Vector3f[] points = new Vector3f[8];
        points[0] = new Vector3f(x, y, z + size);
        points[1] = new Vector3f(x + size, y, z + size);
        points[2] = new Vector3f(x + size, y + size, z + size);
        points[3] = new Vector3f(x, y + size, z + size);
        points[4] = new Vector3f(x + size, y, z);
        points[5] = new Vector3f(x, y, z);
        points[6] = new Vector3f(x, y + size, z);
        points[7] = new Vector3f(x + size, y + size, z);

        // Front face
        if (drawFront) {
            int frontStart = mesh.vertices.size() / 3;
            mesh.addVertex(points[0], 0.0f, 1.0f).addVertex(points[1], 1.0f, 1.0f).addVertex(points[2], 1.0f, 0.0f)
                    .addVertex(points[3], 0.0f, 0.0f);
            mesh.addQuad(frontStart, frontStart + 1, frontStart + 2, frontStart + 3);
        }

        // Back face
        if (drawBack) {
            int backStart = mesh.vertices.size() / 3;
            mesh.addVertex(points[4], 0.0f, 1.0f).addVertex(points[5], 1.0f, 1.0f).addVertex(points[6], 1.0f, 0.0f)
                    .addVertex(points[7], 0.0f, 0.0f);
            mesh.addQuad(backStart, backStart + 1, backStart + 2, backStart + 3);
        }

        // Top face
        if (drawTop) {
            int topStart = mesh.vertices.size() / 3;
            mesh.addVertex(points[3], 0.0f, 0.0f).addVertex(points[2], 1.0f, 0.0f).addVertex(points[7], 1.0f, 1.0f)
                    .addVertex(points[6], 0.0f, 1.0f);
            mesh.addQuad(topStart, topStart + 1, topStart + 2, topStart + 3);
        }

        // Bottom face
        if (drawBottom) {
            int bottomStart = mesh.vertices.size() / 3;
            mesh.addVertex(points[5], 0.0f, 1.0f).addVertex(points[4], 1.0f, 1.0f).addVertex(points[1], 1.0f, 0.0f)
                    .addVertex(points[0], 0.0f, 0.0f);
            mesh.addQuad(bottomStart, bottomStart + 1, bottomStart + 2, bottomStart + 3);
        }

        // Right face
        if (drawRight) {
            int rightStart = mesh.vertices.size() / 3;
            mesh.addVertex(points[1], 0.0f, 1.0f).addVertex(points[4], 1.0f, 1.0f).addVertex(points[7], 1.0f, 0.0f)
                    .addVertex(points[2], 0.0f, 0.0f);
            mesh.addQuad(rightStart, rightStart + 1, rightStart + 2, rightStart + 3);
        }

        // Left face
        if (drawLeft) {
            int leftStart = mesh.vertices.size() / 3;
            mesh.addVertex(points[5], 0.0f, 1.0f).addVertex(points[0], 1.0f, 1.0f).addVertex(points[3], 1.0f, 0.0f)
                    .addVertex(points[6], 0.0f, 0.0f);
            mesh.addQuad(leftStart, leftStart + 1, leftStart + 2, leftStart + 3);
        }

        mesh.finalizeMesh();
    }

    /**
     * Utility method to add multiple voxel models to scene
     */
    public static void addVoxelModelsToScene(Scene scene, List<ModelData> models, List<VoxelModelData> voxelModels) {
        for (VoxelModelData voxelModel : voxelModels) {
            models.add(voxelModel.modelData);
            scene.addEntity(voxelModel.entity);
        }
    }
}
