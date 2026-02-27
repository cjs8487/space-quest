package com.deepwelldevelopment.spacequest.engine.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector4f;

/**
 * Manages materials for voxel-based models.
 * Provides default materials and allows for custom material registration.
 */
public class VoxelMaterialManager {
    
    private static final Map<String, MaterialData> materials = new HashMap<>();
    private static boolean initialized = false;
    
    /**
     * Initializes default voxel materials
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        // Stone materials
        registerMaterial("stone_material", "", new Vector4f(0.5f, 0.5f, 0.5f, 1.0f));
        registerMaterial("cobblestone_material", "", new Vector4f(0.4f, 0.4f, 0.4f, 1.0f));
        
        // Wood materials
        registerMaterial("wood_material", "", new Vector4f(0.6f, 0.4f, 0.2f, 1.0f));
        registerMaterial("log_material", "", new Vector4f(0.5f, 0.3f, 0.1f, 1.0f));
        registerMaterial("planks_material", "", new Vector4f(0.7f, 0.5f, 0.3f, 1.0f));
        
        // Ground materials
        registerMaterial("grass_material", "", new Vector4f(0.2f, 0.6f, 0.2f, 1.0f));
        registerMaterial("dirt_material", "", new Vector4f(0.4f, 0.3f, 0.2f, 1.0f));
        registerMaterial("sand_material", "", new Vector4f(0.8f, 0.7f, 0.5f, 1.0f));
        
        // Building materials
        registerMaterial("brick_material", "", new Vector4f(0.7f, 0.2f, 0.2f, 1.0f));
        registerMaterial("roof_material", "", new Vector4f(0.8f, 0.2f, 0.1f, 1.0f));
        
        // Transparent materials
        registerMaterial("glass_material", "", new Vector4f(0.7f, 0.9f, 1.0f, 0.8f));
        registerMaterial("water_material", "", new Vector4f(0.2f, 0.4f, 0.8f, 0.7f));
        registerMaterial("leaves_material", "", new Vector4f(0.1f, 0.6f, 0.1f, 0.9f));
        
        // Ore materials
        registerMaterial("coal_ore_material", "", new Vector4f(0.2f, 0.2f, 0.2f, 1.0f));
        registerMaterial("iron_ore_material", "", new Vector4f(0.6f, 0.4f, 0.3f, 1.0f));
        registerMaterial("gold_ore_material", "", new Vector4f(0.9f, 0.8f, 0.3f, 1.0f));
        registerMaterial("diamond_ore_material", "", new Vector4f(0.4f, 0.8f, 0.9f, 1.0f));
        
        initialized = true;
    }
    
    /**
     * Registers a custom material
     */
    public static void registerMaterial(String materialId, String texturePath, Vector4f diffuseColor) {
        materials.put(materialId, new MaterialData(materialId, texturePath, diffuseColor));
    }
    
    /**
     * Gets all registered materials
     */
    public static List<MaterialData> getAllMaterials() {
        initialize();
        return new ArrayList<>(materials.values());
    }
    
    /**
     * Gets a specific material by ID
     */
    public static MaterialData getMaterial(String materialId) {
        initialize();
        return materials.get(materialId);
    }
    
    /**
     * Checks if a material is registered
     */
    public static boolean hasMaterial(String materialId) {
        initialize();
        return materials.containsKey(materialId);
    }
    
    /**
     * Gets material for a specific voxel type
     */
    public static MaterialData getMaterialForVoxelType(VoxelType voxelType) {
        if (voxelType == null || voxelType.getMaterialId() == null) {
            return null;
        }
        return getMaterial(voxelType.getMaterialId());
    }
    
    /**
     * Clears all registered materials (useful for testing)
     */
    public static void clear() {
        materials.clear();
        initialized = false;
    }
    
    /**
     * Gets the number of registered materials
     */
    public static int getMaterialCount() {
        initialize();
        return materials.size();
    }
}
