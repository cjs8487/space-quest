package com.deepwelldevelopment.spacequest.engine.model;

/**
 * Enumeration of voxel types for the voxel-based game system.
 * Each voxel type has an associated material ID for rendering.
 */
public enum VoxelType {
    AIR(null, "air"),
    STONE("stone_material", "stone"),
    WOOD("wood_material", "wood"),
    GRASS("grass_material", "grass"),
    DIRT("dirt_material", "dirt"),
    SAND("sand_material", "sand"),
    WATER("water_material", "water"),
    LEAVES("leaves_material", "leaves"),
    LOG("log_material", "log"),
    PLANKS("planks_material", "planks"),
    GLASS("glass_material", "glass"),
    COBBLESTONE("cobblestone_material", "cobblestone"),
    BRICK("brick_material", "brick"),
    COAL_ORE("coal_ore_material", "coal_ore"),
    IRON_ORE("iron_ore_material", "iron_ore"),
    GOLD_ORE("gold_ore_material", "gold_ore"),
    DIAMOND_ORE("diamond_ore_material", "diamond_ore"),
    ROOF("roof_material", "roof");
    
    private final String materialId;
    private final String name;
    
    VoxelType(String materialId, String name) {
        this.materialId = materialId;
        this.name = name;
    }
    
    public String getMaterialId() {
        return materialId;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isSolid() {
        return this != AIR && this != WATER;
    }
    
    public boolean isOpaque() {
        return this != AIR && this != GLASS && this != WATER;
    }
    
    /**
     * Gets a voxel type by name (case-insensitive)
     */
    public static VoxelType byName(String name) {
        if (name == null) return AIR;
        
        for (VoxelType type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return AIR;
    }
    
    /**
     * Gets a voxel type by material ID
     */
    public static VoxelType byMaterialId(String materialId) {
        if (materialId == null) return AIR;
        
        for (VoxelType type : values()) {
            if (materialId.equals(type.materialId)) {
                return type;
            }
        }
        return STONE; // Default fallback
    }
}
