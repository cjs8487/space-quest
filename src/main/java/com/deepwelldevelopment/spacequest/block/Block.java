package com.deepwelldevelopment.spacequest.block;

import org.joml.Vector3f;

import com.deepwelldevelopment.world.World;
import com.deepwelldevelopment.world.chunk.Chunk;

public class Block {

    private final String name;
    private final String materialName;

    public Block(String name, String materialName) {
        this.name = name;
        this.materialName = materialName;
    }

    public String getName() {
        return name;
    }

    public String getMaterialName() {
        return materialName;
    }

    public boolean shouldRenderSide(Chunk chunk, int x, int y, int z, Side side) {
        Block neighbor = side.getBlockAt(chunk, x, y, z);
        return neighbor == null || neighbor == Blocks.AIR;
    }

    public enum Side {
        FRONT(new Vector3f(0, 0, 1)),
        BACK(new Vector3f(0, 0, -1)),
        RIGHT(new Vector3f(1, 0, 0)),
        LEFT(new Vector3f(-1, 0, 0)),
        TOP(new Vector3f(0, 1, 0)),
        BOTTOM(new Vector3f(0, -1, 0));

        private Vector3f sideDirection;

        Side(Vector3f sideDirection) {
            this.sideDirection = sideDirection;
        }

        public Block getBlockAt(Chunk chunk, int x, int y, int z) {
            int neighborX = x + (int) sideDirection.x;
            int neighborY = y + (int) sideDirection.y;
            int neighborZ = z + (int) sideDirection.z;

            if (neighborX >= 0 && neighborX < World.CHUNK_SIZE &&
                    neighborY >= 0 && neighborY < World.CHUNK_SIZE &&
                    neighborZ >= 0 && neighborZ < World.CHUNK_SIZE) {
                return chunk.getBlock(neighborX, neighborY, neighborZ);
            }

            World world = chunk.getWorld();
            if (world != null) {
                int worldX = chunk.getWorldX() * World.CHUNK_SIZE + neighborX;
                int worldZ = chunk.getWorldZ() * World.CHUNK_SIZE + neighborZ;
                return world.getBlock(worldX, neighborY, worldZ);
            }
            return null;
        }
    }
}
