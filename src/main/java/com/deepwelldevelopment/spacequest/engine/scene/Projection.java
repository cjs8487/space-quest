package com.deepwelldevelopment.spacequest.engine.scene;

import org.joml.Matrix4f;

public class Projection {

    private final float fov;
    private final Matrix4f projectionMatrix;
    private final float zFar;
    private final float zNear;

    public Projection(float fov, float zNear, float zFar, int width, int height) {
        this.fov = fov;
        this.zNear = zNear;
        this.zFar = zFar;
        this.projectionMatrix = new Matrix4f();
        this.resize(width, height);
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public float getFov() {
        return fov;
    }

    public float getZFar() {
        return zFar;
    }

    public float getZNear() {
        return zNear;
    }

    public void resize(int width, int height) {
        this.projectionMatrix.identity();
        this.projectionMatrix.perspective(fov, (float) width / height, zNear, zFar, true);
    }
}
