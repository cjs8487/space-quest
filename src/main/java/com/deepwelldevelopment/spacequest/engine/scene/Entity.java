package com.deepwelldevelopment.spacequest.engine.scene;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Entity {

    private final String id;
    private final String modelId;
    private final Matrix4f modelMatrix;
    private final Vector3f position;
    private final Quaternionf rotation;
    private float scale;

    public Entity(String id, String modelId, Vector3f position) {
        this.id = id;
        this.modelId = modelId;
        this.position = position;
        this.scale = 1.0f;
        this.rotation = new Quaternionf();
        this.modelMatrix = new Matrix4f();
        this.updateModelMatrix();
    }

    public String getId() {
        return id;
    }

    public String getModelId() {
        return modelId;
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Quaternionf getRotation() {
        return rotation;
    }

    public float getScale() {
        return scale;
    }

    public void resetRotation() {
        rotation.x = 0.0f;
        rotation.y = 0.0f;
        rotation.z = 0.0f;
        rotation.w = 1.0f;
    }

    public final void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        this.updateModelMatrix();
    }

    public final void setScale(float scale) {
        this.scale = scale;
        this.updateModelMatrix();
    }

    public void updateModelMatrix() {
        this.modelMatrix.translationRotateScale(position, rotation, scale);
    }

}
