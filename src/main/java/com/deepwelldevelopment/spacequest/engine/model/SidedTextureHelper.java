package com.deepwelldevelopment.spacequest.engine.model;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector4f;

import com.deepwelldevelopment.spacequest.block.Block.Side;

/**
 * Helper class for creating and managing sided texture materials
 */
public class SidedTextureHelper {

    /**
     * Creates a sided material configuration for common block types like grass, wood, etc.
     */
    public static Map<Side, String> createGrassTextures(String grassPath, String dirtPath, String grassSidePath) {
        Map<Side, String> textures = new HashMap<>();
        textures.put(Side.TOP, grassPath);
        textures.put(Side.BOTTOM, dirtPath);
        textures.put(Side.FRONT, grassSidePath);
        textures.put(Side.BACK, grassSidePath);
        textures.put(Side.LEFT, grassSidePath);
        textures.put(Side.RIGHT, grassSidePath);
        return textures;
    }

    /**
     * Creates a sided material configuration for log blocks (different texture on top/bottom vs sides)
     */
    public static Map<Side, String> createLogTextures(String sidePath, String topBottomPath) {
        Map<Side, String> textures = new HashMap<>();
        textures.put(Side.TOP, topBottomPath);
        textures.put(Side.BOTTOM, topBottomPath);
        textures.put(Side.FRONT, sidePath);
        textures.put(Side.BACK, sidePath);
        textures.put(Side.LEFT, sidePath);
        textures.put(Side.RIGHT, sidePath);
        return textures;
    }

    /**
     * Creates a sided material configuration for furnace-like blocks (different front texture)
     */
    public static Map<Side, String> createFurnaceTextures(String sidePath, String frontPath, String topPath) {
        Map<Side, String> textures = new HashMap<>();
        textures.put(Side.TOP, topPath);
        textures.put(Side.BOTTOM, sidePath);
        textures.put(Side.FRONT, frontPath);
        textures.put(Side.BACK, sidePath);
        textures.put(Side.LEFT, sidePath);
        textures.put(Side.RIGHT, sidePath);
        return textures;
    }

    /**
     * Registers a grass block material with sided textures
     */
    public static void registerGrassMaterial(String materialId, String grassPath, String dirtPath, String grassSidePath) {
        Map<Side, String> textures = createGrassTextures(grassPath, dirtPath, grassSidePath);
        VoxelMaterialManager.registerSidedMaterial(materialId, grassPath, new Vector4f(0.2f, 0.6f, 0.2f, 1.0f), textures);
    }

    /**
     * Registers a log block material with sided textures
     */
    public static void registerLogMaterial(String materialId, String sidePath, String topBottomPath) {
        Map<Side, String> textures = createLogTextures(sidePath, topBottomPath);
        VoxelMaterialManager.registerSidedMaterial(materialId, sidePath, new Vector4f(0.5f, 0.3f, 0.1f, 1.0f), textures);
    }

    /**
     * Registers a furnace block material with sided textures
     */
    public static void registerFurnaceMaterial(String materialId, String sidePath, String frontPath, String topPath) {
        Map<Side, String> textures = createFurnaceTextures(sidePath, frontPath, topPath);
        VoxelMaterialManager.registerSidedMaterial(materialId, sidePath, new Vector4f(0.4f, 0.4f, 0.4f, 1.0f), textures);
    }
}
