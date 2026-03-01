package com.deepwelldevelopment.world.chunk;

import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.block.Blocks;
import com.deepwelldevelopment.world.World;

/**
 * Represents a 16x16 slice of the world, which spans the entire vertical column
 * of the world.
 */
public class Chunk {

    private final int worldX;
    private final int worldZ;
    private final Block[][][] blocks;
    private ChunkMesh chunkMesh;
    private World world; // Reference to the world for cross-chunk neighbor checking

    public Chunk(int worldX, int worldZ) {
        this.worldX = worldX;
        this.worldZ = worldZ;
        this.blocks = new Block[World.CHUNK_SIZE][World.CHUNK_SIZE][World.CHUNK_SIZE];
    }

    public void createMesh() {
        this.chunkMesh = new ChunkMesh(this);
    }

    public Block getBlock(int x, int y, int z) {
        if (x < 0 || x >= World.CHUNK_SIZE || y < 0 || y >= World.CHUNK_SIZE || z < 0 || z >= World.CHUNK_SIZE) {
            return null;
        }
        return blocks[x][y][z];
    }

    public void setBlock(int x, int y, int z, Block block) {
        blocks[x][y][z] = block;
    }

    public void generate() {
        // TODO: Implement real chunk generation
        // Create simple terrain: stone at bottom 2 levels, air above
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int y = 0; y < World.CHUNK_SIZE; y++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    if (y % 2 == 0) {
                        blocks[x][y][z] = Blocks.STONE;
                    } else {
                        blocks[x][y][z] = Blocks.DIRT;
                    }
                }
            }
        }
    }

    public int getWorldX() {
        return worldX;
    }

    public int getWorldZ() {
        return worldZ;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public ChunkMesh getChunkMesh() {
        return chunkMesh;
    }
}
