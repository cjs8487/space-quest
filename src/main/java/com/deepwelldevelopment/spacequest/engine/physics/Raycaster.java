package com.deepwelldevelopment.spacequest.engine.physics;

import org.joml.Vector3f;

import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.block.Blocks;
import com.deepwelldevelopment.spacequest.world.World;

public class Raycaster {

    private static final float MAX_DISTANCE = 100.0f;
    private static final float EPSILON = 0.001f;

    public static RaycastResult raycast(World world, Ray ray) {
        return raycast(world, ray, MAX_DISTANCE);
    }

    public static RaycastResult raycast(World world, Ray ray, float maxDistance) {
        Vector3f pos = new Vector3f(ray.origin);
        Vector3f dir = new Vector3f(ray.direction);

        // Current voxel coordinates
        int x = (int) Math.floor(pos.x);
        int y = (int) Math.floor(pos.y);
        int z = (int) Math.floor(pos.z);

        // Step direction (+1 or -1 for each axis)
        int stepX = dir.x > 0 ? 1 : -1;
        int stepY = dir.y > 0 ? 1 : -1;
        int stepZ = dir.z > 0 ? 1 : -1;

        // Distance to next voxel boundary along each axis
        float tMaxX = getNextVoxelBoundary(pos.x, dir.x, stepX);
        float tMaxY = getNextVoxelBoundary(pos.y, dir.y, stepY);
        float tMaxZ = getNextVoxelBoundary(pos.z, dir.z, stepZ);

        // How far we move along ray to cross one voxel
        float tDeltaX = dir.x == 0 ? Float.MAX_VALUE : Math.abs(1.0f / dir.x);
        float tDeltaY = dir.y == 0 ? Float.MAX_VALUE : Math.abs(1.0f / dir.y);
        float tDeltaZ = dir.z == 0 ? Float.MAX_VALUE : Math.abs(1.0f / dir.z);

        Vector3f normal = new Vector3f();
        float distance = 0.0f;

        while (distance <= maxDistance) {
            // Check current voxel
            Block block = world.getBlock(x, y, z);
            if (block != null && block != Blocks.AIR) {
                Vector3f hitPos = new Vector3f(pos);
                Vector3f blockPos = new Vector3f(x, y, z);
                return new RaycastResult(block, hitPos, normal, blockPos, distance);
            }

            // Determine which axis to step along
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    // Step in X direction
                    x += stepX;
                    distance = tMaxX;
                    tMaxX += tDeltaX;
                    normal.set(-stepX, 0, 0); // Normal points opposite to movement direction
                } else {
                    // Step in Z direction
                    z += stepZ;
                    distance = tMaxZ;
                    tMaxZ += tDeltaZ;
                    normal.set(0, 0, -stepZ); // Normal points opposite to movement direction
                }
            } else {
                if (tMaxY < tMaxZ) {
                    // Step in Y direction
                    y += stepY;
                    distance = tMaxY;
                    tMaxY += tDeltaY;
                    normal.set(0, -stepY, 0); // Normal points opposite to movement direction
                } else {
                    // Step in Z direction
                    z += stepZ;
                    distance = tMaxZ;
                    tMaxZ += tDeltaZ;
                    normal.set(0, 0, -stepZ); // Normal points opposite to movement direction
                }
            }

            // Update ray position
            pos.set(ray.getPoint(distance));
        }

        return RaycastResult.noHit();
    }

    private static float getNextVoxelBoundary(float pos, float dir, int step) {
        if (dir == 0)
            return Float.MAX_VALUE;

        float boundary;
        if (step > 0) {
            boundary = (float) Math.ceil(pos + EPSILON);
        } else {
            boundary = (float) Math.floor(pos - EPSILON);
        }

        return Math.abs((boundary - pos) / dir);
    }
}
