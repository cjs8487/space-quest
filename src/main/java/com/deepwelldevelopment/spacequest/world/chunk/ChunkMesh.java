package com.deepwelldevelopment.spacequest.world.chunk;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import com.deepwelldevelopment.spacequest.engine.model.VoxelModelFactory;
import com.deepwelldevelopment.spacequest.engine.model.VoxelModelFactory.VoxelModelData;
import com.deepwelldevelopment.spacequest.world.World;

public class ChunkMesh {

    private Chunk chunk;
    private List<VoxelModelData> voxelModels = new ArrayList<>();

    public ChunkMesh(Chunk chunk) {
        this.chunk = chunk;
        this.calculateMesh();
    }

    public List<VoxelModelData> getVoxelModels() {
        return voxelModels;
    }

    public void calculateMesh() {
        try {
            String modelId = "chunk_" + chunk.getWorldX() + "_" + chunk.getWorldZ();
            VoxelModelFactory.VoxelModelData voxelModel = VoxelModelFactory.createFromBlocksGreedy(modelId, chunk,
                    new Vector3f(chunk.getWorldX() * World.CHUNK_SIZE, 0, chunk.getWorldZ() * World.CHUNK_SIZE));

            voxelModels.clear();

            if (voxelModel != null) {
                voxelModels.add(voxelModel);
            } else {
                System.out.println(
                        "Chunk at (" + chunk.getWorldX() + ", " + chunk.getWorldZ() + ") has no visible faces");
            }
        } catch (Exception e) {
            System.err.println("Failed to create greedy mesh for chunk at (" + chunk.getWorldX() + ", "
                    + chunk.getWorldZ() + "): " + e.getMessage());
            e.printStackTrace();
        }
    }
}
