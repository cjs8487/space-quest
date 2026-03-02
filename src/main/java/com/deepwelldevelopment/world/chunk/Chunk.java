package com.deepwelldevelopment.world.chunk;

import java.util.Arrays;

import org.spongepowered.noise.module.source.Perlin;

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

    private Perlin perlin;
    private int[][] generationHeightmap;

    public Chunk(int worldX, int worldZ, int seed) {
        this.worldX = worldX;
        this.worldZ = worldZ;
        this.blocks = new Block[World.CHUNK_SIZE][World.CHUNK_SIZE][World.CHUNK_SIZE];

        this.perlin = new Perlin();
        this.perlin.setSeed(seed);
        this.perlin.setFrequency(0.15);
        this.perlin.setLacunarity(2);
        this.perlin.setOctaveCount(6);
        this.perlin.setPersistence(0.5);
        this.generationHeightmap = new int[World.CHUNK_SIZE][World.CHUNK_SIZE];
        for (int[] arr : generationHeightmap) {
            Arrays.fill(arr, 8);
        }
    }

    public void createMesh() {
        try {
            this.chunkMesh = new ChunkMesh(this);
        } catch (Exception e) {
            System.err
                    .println("Failed to create mesh for chunk at (" + worldX + ", " + worldZ + "): " + e.getMessage());
            e.printStackTrace();
            // Set chunkMesh to null to indicate mesh creation failed
            this.chunkMesh = null;
        }
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
        double xOff = worldX * World.CHUNK_SIZE * 0.005;
        double zOff = worldZ * World.CHUNK_SIZE * 0.005;
        double startZ = zOff;
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int z = World.CHUNK_SIZE - 1; z >= 0; z--) {
                generationHeightmap[x][z] += (4 * perlin.get(xOff + x * 0.005, zOff + z * 0.005, 0));
            }
            xOff += 0.005;
            zOff = startZ;
        }

        for (int y = 0; y < World.CHUNK_SIZE; y++) {
            for (int x = 0; x < World.CHUNK_SIZE; x++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    int height = generationHeightmap[x][z];
                    if (y == height) {
                        blocks[x][y][z] = Blocks.GRASS;
                    } else if (y < height) {
                        blocks[x][y][z] = Blocks.STONE;
                    } else {
                        blocks[x][y][z] = Blocks.AIR;
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
