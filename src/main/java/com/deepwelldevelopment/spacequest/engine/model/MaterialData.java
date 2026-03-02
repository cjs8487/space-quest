package com.deepwelldevelopment.spacequest.engine.model;

import java.util.Map;
import java.util.HashMap;

import org.joml.Vector2f;
import org.joml.Vector4f;

import com.deepwelldevelopment.spacequest.block.Block.Side;

public record MaterialData(String id, String texturePath, Vector4f diffuseColor, Vector2f uvScale, Vector2f uvOffset,
        Map<Side, String> sideTextures) {

    public MaterialData(String id, String texturePath, Vector4f diffuseColor) {
        this(id, texturePath, diffuseColor, new Vector2f(1.0f, 1.0f), new Vector2f(0.0f, 0.0f), new HashMap<>());
    }

    public MaterialData(String id, String texturePath, Vector4f diffuseColor, Vector2f uvScale) {
        this(id, texturePath, diffuseColor, uvScale, new Vector2f(0.0f, 0.0f), new HashMap<>());
    }

    public MaterialData(String id, String texturePath, Vector4f diffuseColor, Vector2f uvScale, Vector2f uvOffset) {
        this(id, texturePath, diffuseColor, uvScale, uvOffset, new HashMap<>());
    }

    public MaterialData(String id, String texturePath, Vector4f diffuseColor, Vector2f uvScale, Vector2f uvOffset,
            Map<Side, String> sideTextures) {
        this.id = id;
        this.texturePath = texturePath;
        this.diffuseColor = diffuseColor;
        this.uvScale = uvScale;
        this.uvOffset = uvOffset;
        this.sideTextures = sideTextures != null ? new HashMap<>(sideTextures) : new HashMap<>();
    }

    public String getTexturePath(Side side) {
        return sideTextures.containsKey(side) ? sideTextures.get(side) : texturePath;
    }

    public boolean hasSidedTextures() {
        return !sideTextures.isEmpty();
    }
}
