package com.deepwelldevelopment.spacequest.engine.model;

import org.joml.Vector2f;
import org.joml.Vector4f;

public record MaterialData(String id, String texturePath, Vector4f diffuseColor, Vector2f uvScale, Vector2f uvOffset) {

    public MaterialData(String id, String texturePath, Vector4f diffuseColor) {
        this(id, texturePath, diffuseColor, new Vector2f(1.0f, 1.0f), new Vector2f(0.0f, 0.0f));
    }

    public MaterialData(String id, String texturePath, Vector4f diffuseColor, Vector2f uvScale) {
        this(id, texturePath, diffuseColor, uvScale, new Vector2f(0.0f, 0.0f));
    }
}
