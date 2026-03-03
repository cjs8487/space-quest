package com.deepwelldevelopment.spacequest.engine.physics;

import org.joml.Vector3f;

public class Ray {
    public final Vector3f origin;
    public final Vector3f direction;
    
    public Ray(Vector3f origin, Vector3f direction) {
        this.origin = new Vector3f(origin);
        this.direction = new Vector3f(direction).normalize();
    }
    
    public Vector3f getPoint(float t) {
        return new Vector3f(origin).add(direction.x * t, direction.y * t, direction.z * t);
    }
}
