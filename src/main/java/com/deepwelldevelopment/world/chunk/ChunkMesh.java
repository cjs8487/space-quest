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
        // Use greedy meshing for better performance
        voxelModels.add(VoxelModelFactory.createFromBlocksGreedy("chunk_" + chunk.getWorldX() + "_" + chunk.getWorldZ(),
                chunk, new Vector3f(chunk.getWorldX() * World.CHUNK_SIZE, 0, chunk.getWorldZ() * World.CHUNK_SIZE)));
    }

    public List<VoxelModelData> getVoxelModels() {
        return voxelModels;
    }
}
