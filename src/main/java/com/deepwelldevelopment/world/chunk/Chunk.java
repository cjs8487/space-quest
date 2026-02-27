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

    public Chunk(int worldX, int worldZ) {
        this.worldX = worldX;
        this.worldZ = worldZ;
        this.blocks = new Block[World.CHUNK_SIZE][World.CHUNK_SIZE][World.CHUNK_SIZE];
    }

    public void createMesh() {
        this.chunkMesh = new ChunkMesh(this);
    }

    public Block getBlock(int x, int y, int z) {
        return blocks[x][y][z];
    }

    public void setBlock(int x, int y, int z, Block block) {
        blocks[x][y][z] = block;
    }

    public void generate() {
        // TODO: Implement real chunk generation
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int y = 0; y < World.CHUNK_SIZE; y++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    blocks[x][y][z] = Blocks.STONE;
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

    public ChunkMesh getChunkMesh() {
        return chunkMesh;
    }
}
