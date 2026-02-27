package com.deepwelldevelopment.spacequest.block;

public class Block {

    private final String name;
    private final String materialName;

    public Block(String name, String materialName) {
        this.name = name;
        this.materialName = materialName;
    }

    public String getName() {
        return name;
    }

    public String getMaterialName() {
        return materialName;
    }
}
