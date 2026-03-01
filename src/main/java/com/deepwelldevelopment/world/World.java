package com.deepwelldevelopment.world;

import java.util.ArrayList;
import java.util.List;

import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.world.chunk.Chunk;

public class World {

    public static final int CHUNK_SIZE = 16;

    private static final int WORLD_SIZE = 2;

    Chunk[][] chunks;

    public World() {
        this.chunks = new Chunk[WORLD_SIZE][WORLD_SIZE];
    }

    public Chunk getChunk(int x, int z) {
        int chunkX = x / CHUNK_SIZE;
        int chunkZ = z / CHUNK_SIZE;

        if (chunkX < 0 || chunkX >= WORLD_SIZE || chunkZ < 0 || chunkZ >= WORLD_SIZE) {
            return null;
        }

        return chunks[chunkX][chunkZ];
    }

    public Block getBlock(int x, int y, int z) {
        if (x < 0 || x >= WORLD_SIZE * CHUNK_SIZE || y < 0 || y >= CHUNK_SIZE || z < 0
                || z >= WORLD_SIZE * CHUNK_SIZE) {
            return null;
        }

        int chunkX = x / CHUNK_SIZE;
        int chunkZ = z / CHUNK_SIZE;

        if (chunkX < 0 || chunkX >= WORLD_SIZE || chunkZ < 0 || chunkZ >= WORLD_SIZE) {
            return null;
        }

        int localX = x % CHUNK_SIZE;
        int localY = y; // Y is already local since we don't stack chunks vertically
        int localZ = z % CHUNK_SIZE;

        Chunk chunk = chunks[chunkX][chunkZ];
        if (chunk == null) {
            return null;
        }
        return chunk.getBlock(localX, localY, localZ);
    }

    public void generate() {
        // TODO: Implement noise based world generation

        for (int x = 0; x < WORLD_SIZE; x++) {
            for (int z = 0; z < WORLD_SIZE; z++) {
                Chunk chunk = new Chunk(x, z);
                chunk.setWorld(this); // Set world reference for cross-chunk neighbor checking
                chunk.generate();
                chunks[x][z] = chunk; // Add chunk to array before mesh creation
                chunk.createMesh(); // Create mesh after generation
            }
        }
    }

    public List<Chunk> getChunks() {
        List<Chunk> chunksList = new ArrayList<>();
        for (int x = 0; x < WORLD_SIZE; x++) {
            for (int z = 0; z < WORLD_SIZE; z++) {
                chunksList.add(chunks[x][z]);
            }
        }
        return chunksList;
    }
}
