package com.deepwelldevelopment.world.chunk;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import com.deepwelldevelopment.spacequest.engine.model.VoxelModelFactory;
import com.deepwelldevelopment.spacequest.engine.model.VoxelModelFactory.VoxelModelData;
import com.deepwelldevelopment.world.World;

public class ChunkMesh {

    private Chunk chunk;
    private List<VoxelModelData> voxelModels = new ArrayList<>();

    public ChunkMesh(Chunk chunk) {
        this.chunk = chunk;
        try {
            // Use greedy meshing for better performance
            VoxelModelFactory.VoxelModelData voxelModel = VoxelModelFactory.createFromBlocksGreedy(
                    "chunk_" + chunk.getWorldX() + "_" + chunk.getWorldZ(), chunk,
                    new Vector3f(chunk.getWorldX() * World.CHUNK_SIZE, 0, chunk.getWorldZ() * World.CHUNK_SIZE));

            // Only add the model if it's not null (chunk has visible faces)
            if (voxelModel != null) {
                voxelModels.add(voxelModel);
            } else {
                // Debug: Chunk is empty (no visible faces)
                System.out.println(
                        "Chunk at (" + chunk.getWorldX() + ", " + chunk.getWorldZ() + ") has no visible faces");
            }
        } catch (Exception e) {
            System.err.println("Failed to create greedy mesh for chunk at (" + chunk.getWorldX() + ", "
                    + chunk.getWorldZ() + "): " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<VoxelModelData> getVoxelModels() {
        return voxelModels;
    }
}
