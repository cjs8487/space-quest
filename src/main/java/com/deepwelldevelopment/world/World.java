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

    public Chunk getChunk(int x, int y) {
        return chunks[x / WORLD_SIZE][y / WORLD_SIZE];
    }

    public Block getBlock(int x, int y, int z) {
        int localX = x % CHUNK_SIZE;
        int localY = y % CHUNK_SIZE;
        int localZ = z % CHUNK_SIZE;

        if (x < 0 || x > WORLD_SIZE * CHUNK_SIZE || z < 0 || z > WORLD_SIZE * CHUNK_SIZE) {
            return null;
        }

        return getChunk(x, z).getBlock(localX, localY, localZ);
    }

    public void generate() {
        // TODO: Implement noise based world generation

        for (int x = 0; x < WORLD_SIZE; x++) {
            for (int z = 0; z < WORLD_SIZE; z++) {
                Chunk chunk = new Chunk(x, z);
                chunk.generate();
                chunk.createMesh(); // Create mesh after generation
                chunks[x][z] = chunk;
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
