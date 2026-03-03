package com.deepwelldevelopment.spacequest.engine.physics;

import org.joml.Vector3f;

import com.deepwelldevelopment.spacequest.block.Block;

public class RaycastResult {
    public final Block hitBlock;
    public final Vector3f hitPosition;
    public final Vector3f hitNormal;
    public final Vector3f blockPosition;
    public final float distance;
    public final boolean hit;
    
    public RaycastResult(Block hitBlock, Vector3f hitPosition, Vector3f hitNormal, Vector3f blockPosition, float distance) {
        this.hitBlock = hitBlock;
        this.hitPosition = hitPosition;
        this.hitNormal = hitNormal;
        this.blockPosition = blockPosition;
        this.distance = distance;
        this.hit = hitBlock != null;
    }
    
    public static RaycastResult noHit() {
        return new RaycastResult(null, null, null, null, Float.MAX_VALUE);
    }
}
