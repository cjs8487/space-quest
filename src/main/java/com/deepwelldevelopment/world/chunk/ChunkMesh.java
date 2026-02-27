package com.deepwelldevelopment.world.chunk;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.engine.model.VoxelModelFactory;
import com.deepwelldevelopment.spacequest.engine.model.VoxelModelFactory.VoxelModelData;
import com.deepwelldevelopment.world.World;

public class ChunkMesh {

    private Chunk chunk;
    private List<VoxelModelData> voxelModels = new ArrayList<>();

    public ChunkMesh(Chunk chunk) {
        this.chunk = chunk;
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int y = 0; y < World.CHUNK_SIZE; y++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (block != null) {
                        float worldX = chunk.getWorldX() * World.CHUNK_SIZE + x;
                        float worldZ = chunk.getWorldZ() * World.CHUNK_SIZE + z;
                        voxelModels.add(VoxelModelFactory.createBlock("block_" + x + "_" + y + "_" + z,
                                block.getMaterialName(), new Vector3f(worldX, y, worldZ)));
                    }
                }
            }
        }
    }

    public List<VoxelModelData> getVoxelModels() {
        return voxelModels;
    }
}
