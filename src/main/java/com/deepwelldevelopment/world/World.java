package com.deepwelldevelopment.world;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.world.chunk.Chunk;

public class World {

    public static final int CHUNK_SIZE = 16;

    private static final int VIEW_DISTANCE = 4;

    private Map<Long, Chunk> chunks;

    public World() {
        this.chunks = new HashMap<Long, Chunk>();
    }

    public Chunk getChunk(int x, int z) {
        int chunkX = Math.floorDiv(x, CHUNK_SIZE);
        int chunkZ = Math.floorDiv(z, CHUNK_SIZE);

        return chunks.get(((long) chunkX << 32) | (chunkZ & 0xffffffffL));
    }

    public Block getBlock(int x, int y, int z) {
        int localX = (x & 15);
        int localY = y; // Y is already local since we don't stack chunks vertically
        int localZ = (z & 15);

        Chunk chunk = getChunk(x, z);
        if (chunk == null) {
            return null;
        }
        return chunk.getBlock(localX, localY, localZ);
    }

    public void generate() {
        // TODO: Implement noise based world generation

        for (int xc = -VIEW_DISTANCE; xc <= VIEW_DISTANCE; xc++) {
            for (int zc = -VIEW_DISTANCE; zc <= VIEW_DISTANCE; zc++) {
                if (Math.abs(xc) + Math.abs(zc) > VIEW_DISTANCE)
                    continue;
                long chunkKey = ((long) xc << 32) | (zc & 0xffffffffL);
                Chunk chunk = chunks.get(chunkKey);
                if (chunk == null) {
                    chunk = new Chunk(xc, zc);
                    chunk.setWorld(this);
                    chunk.generate();
                    chunk.createMesh();
                    chunks.put(chunkKey, chunk);
                }
            }
        }
    }

    public Collection<Chunk> getChunks() {
        return chunks.values();
    }
}
