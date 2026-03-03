package com.deepwelldevelopment.spacequest.engine.scene;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {

    private final Vector3f direction;
    private final Vector3f position;
    private final Vector3f right;
    private final Vector2f rotation;
    private final Matrix4f viewMatrix;

    public Camera() {
        direction = new Vector3f();
        right = new Vector3f();
        position = new Vector3f(0, 0, 0);
        viewMatrix = new Matrix4f();
        rotation = new Vector2f();
    }

    public void addRotation(float x, float y) {
        rotation.x += x;
        rotation.x = Math.max((float) -Math.PI / 2, Math.min((float) Math.PI / 2, rotation.x));
        rotation.y += y;
        recalculate();
    }

    public Vector3f getPosition() {
        return position;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public void moveForward(float inc) {
        viewMatrix.positiveZ(direction).negate();
        direction.y = 0;
        direction.normalize();
        position.add(direction.x * inc, 0, direction.z * inc);
        recalculate();
    }

    public void moveBackwards(float inc) {
        viewMatrix.positiveZ(direction).negate();
        direction.y = 0;
        direction.normalize();
        position.sub(direction.x * inc, 0, direction.z * inc);
        recalculate();
    }

    public void moveLeft(float inc) {
        viewMatrix.positiveX(right);
        right.y = 0;
        right.normalize();
        position.sub(right.x * inc, 0, right.z * inc);
        recalculate();
    }

    public void moveRight(float inc) {
        viewMatrix.positiveX(right);
        right.y = 0;
        right.normalize();
        position.add(right.x * inc, 0, right.z * inc);
        recalculate();
    }

    public void moveUp(float inc) {
        position.y += inc;
        recalculate();
    }

    public void moveDown(float inc) {
        position.y -= inc;
        recalculate();
    }

    private void recalculate() {
        viewMatrix.identity().rotateX(rotation.x).rotateY(rotation.y).translate(-position.x, -position.y, -position.z);
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        recalculate();
    }

    public void setRotation(float x, float y) {
        rotation.set(Math.max((float) -Math.PI / 2, Math.min((float) Math.PI / 2, x)), y);
        recalculate();
    }
}
