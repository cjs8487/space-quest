package com.deepwelldevelopment.spacequest.engine.model;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModelDataRegistry {
    private static final Map<String, byte[]> vertexDataMap = new ConcurrentHashMap<>();
    private static final Map<String, byte[]> indexDataMap = new ConcurrentHashMap<>();

    public static void registerModel(String modelId, byte[] vertexData, byte[] indexData) {
        vertexDataMap.put(modelId, vertexData);
        indexDataMap.put(modelId, indexData);
    }

    public static DataInputStream getVertexInputStream(String modelId) {
        byte[] data = vertexDataMap.get(modelId);
        if (data == null) {
            throw new RuntimeException("Vertex data not found for model: " + modelId);
        }
        return new DataInputStream(new ByteArrayInputStream(data));
    }

    public static DataInputStream getIndexInputStream(String modelId) {
        byte[] data = indexDataMap.get(modelId);
        if (data == null) {
            throw new RuntimeException("Index data not found for model: " + modelId);
        }
        return new DataInputStream(new ByteArrayInputStream(data));
    }

    public static boolean hasModel(String modelId) {
        return vertexDataMap.containsKey(modelId) && indexDataMap.containsKey(modelId);
    }

    public static void clearModel(String modelId) {
        vertexDataMap.remove(modelId);
        indexDataMap.remove(modelId);
    }

    public static void clearAll() {
        vertexDataMap.clear();
        indexDataMap.clear();
    }
}
