package com.deepwelldevelopment.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.joml.Vector3f;

import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.engine.graph.Renderer;
import com.deepwelldevelopment.spacequest.engine.model.ModelData;
import com.deepwelldevelopment.spacequest.engine.scene.Scene;
import com.deepwelldevelopment.world.chunk.Chunk;
import com.deepwelldevelopment.world.chunk.ChunkMesh;

public class World {

    public static final int CHUNK_SIZE = 16;

    private static final int VIEW_DISTANCE = 4;

    private Map<Long, Chunk> chunks;
    private Renderer renderer;
    private Scene scene;
    private int worldSeed;

    public World() {
        this.chunks = new HashMap<Long, Chunk>();
        this.worldSeed = new Random().nextInt();
    }

    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
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
        // Generate initial chunks around origin
        Vector3f origin = new Vector3f(0, 0, 0);
        int radius = CHUNK_SIZE * VIEW_DISTANCE;
        for (int xc = -radius; xc <= radius; xc += CHUNK_SIZE) {
            for (int zc = -radius; zc <= radius; zc += CHUNK_SIZE) {
                if (xc * xc + zc * zc <= radius * radius) {
                    checkAndCreateChunk(origin, xc, zc);
                }
            }
        }
    }

    public Collection<Chunk> getChunks() {
        return chunks.values();
    }

    public void tick(Vector3f cameraPosition) {
        int radius = CHUNK_SIZE * VIEW_DISTANCE;
        for (int xc = -radius; xc <= radius; xc += CHUNK_SIZE) {
            for (int zc = -radius; zc <= radius; zc += CHUNK_SIZE) {
                if (xc * xc + zc * zc <= radius * radius) {
                    checkAndCreateChunk(cameraPosition, xc, zc);
                }
            }
        }

        // Remove chunks that are too far away
        boolean hadChunksToRemove = cleanupDistantChunks(cameraPosition);

        // Only trigger cleanup if we actually removed chunks
        if (hadChunksToRemove && renderer != null) {
            renderer.triggerCleanup();
        }
    }

    private void checkAndCreateChunk(Vector3f camPos, int xc, int zc) {
        // Calculate world position for this chunk
        int worldX = (int) Math.floor((camPos.x + xc) / CHUNK_SIZE);
        int worldZ = (int) Math.floor((camPos.z + zc) / CHUNK_SIZE);

        Chunk chunk = getChunk(worldX * CHUNK_SIZE, worldZ * CHUNK_SIZE);
        if (chunk == null) {
            long chunkKey = ((long) worldX << 32) | (worldZ & 0xffffffffL);
            chunk = new Chunk(worldX, worldZ, worldSeed);
            chunk.setWorld(this);
            chunk.generate();
            chunk.createMesh();
            chunks.put(chunkKey, chunk);

            // Notify renderer of new chunk models and add entities to scene
            if (renderer != null && scene != null) {
                ChunkMesh chunkMesh = chunk.getChunkMesh();
                if (chunkMesh != null) {
                    List<ModelData> models = new ArrayList<>();
                    // Add model data and entities
                    for (var voxelModel : chunkMesh.getVoxelModels()) {
                        models.add(voxelModel.modelData);
                        scene.addEntity(voxelModel.entity);
                    }
                    for (ModelData modelData : models) {
                        renderer.addModel(modelData);
                    }
                }
            }
        }
    }

    private boolean cleanupDistantChunks(Vector3f cameraPosition) {
        int maxDistance = CHUNK_SIZE * (VIEW_DISTANCE + 2); // Add buffer to avoid constant loading/unloading
        List<Long> chunksToRemove = new ArrayList<>();
        boolean hadChunksToRemove = false;

        for (Map.Entry<Long, Chunk> entry : chunks.entrySet()) {
            Chunk chunk = entry.getValue();
            float distance = Math.max(Math.abs(chunk.getWorldX() * CHUNK_SIZE - cameraPosition.x),
                    Math.abs(chunk.getWorldZ() * CHUNK_SIZE - cameraPosition.z));

            if (distance > maxDistance) {
                chunksToRemove.add(entry.getKey());
                hadChunksToRemove = true;

                // Remove models from renderer and entities from scene
                if (renderer != null && scene != null && chunk.getChunkMesh() != null) {
                    ChunkMesh chunkMesh = chunk.getChunkMesh();
                    if (chunkMesh != null) {
                        for (var voxelModel : chunkMesh.getVoxelModels()) {
                            renderer.removeModel(voxelModel.modelData.id());
                            scene.removeEntity(voxelModel.entity.getId());
                        }
                    }
                }
            }
        }

        // Remove distant chunks from the map
        for (Long chunkKey : chunksToRemove) {
            chunks.remove(chunkKey);
        }

        return hadChunksToRemove;
    }
}
