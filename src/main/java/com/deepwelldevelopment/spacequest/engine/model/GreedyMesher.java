package com.deepwelldevelopment.spacequest.engine.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.block.Blocks;
import com.deepwelldevelopment.spacequest.block.Block.Side;
import com.deepwelldevelopment.spacequest.engine.model.ProgrammaticModel.ProgrammaticMesh;
import com.deepwelldevelopment.spacequest.world.World;
import com.deepwelldevelopment.spacequest.world.chunk.Chunk;

/**
 * Greedy meshing implementation for optimizing chunk rendering. Combines
 * adjacent faces of the same block type into larger quads.
 */
public class GreedyMesher {

    public static class GreedyMeshResult {
        public final ProgrammaticModel model;
        public final int faceCount;
        public final int vertexCount;

        public GreedyMeshResult(ProgrammaticModel model, int faceCount, int vertexCount) {
            this.model = model;
            this.faceCount = faceCount;
            this.vertexCount = vertexCount;
        }
    }

    private static class FaceMeshResult {
        final List<Float> vertices = new ArrayList<>();
        final List<Float> texCoords = new ArrayList<>();
        final List<Integer> indices = new ArrayList<>();
        int faceCount = 0;
        int vertexCount = 0;
    }

    /**
     * Creates an optimized mesh using greedy meshing algorithm
     */
    public static GreedyMeshResult createGreedyMesh(String id, Chunk chunk) {
        ProgrammaticModel model = new ProgrammaticModel(id);
        int totalFaces = 0;
        int totalVertices = 0;

        // Process each face direction separately
        for (Side side : Side.values()) {
            Map<String, FaceMeshResult> materialResults = meshFaceByMaterial(chunk, side);

            for (Map.Entry<String, FaceMeshResult> entry : materialResults.entrySet()) {
                String materialName = entry.getKey();
                FaceMeshResult result = entry.getValue();

                if (result.faceCount > 0) {
                    // Use side-specific material name if the material has sided textures
                    String meshMaterialName = getMaterialNameForSide(materialName, side);
                    ProgrammaticMesh mesh = model.addMesh(id + "_" + materialName + "_" + side.name(),
                            meshMaterialName);
                    mesh.vertices.addAll(result.vertices);
                    mesh.texCoords.addAll(result.texCoords);
                    mesh.indices.addAll(result.indices);
                    mesh.finalizeMesh();
                    totalFaces += result.faceCount;
                    totalVertices += result.vertexCount;
                }
            }
        }

        return new GreedyMeshResult(model, totalFaces, totalVertices);
    }

    /**
     * Gets the appropriate material name for a specific side, handling sided
     * textures
     */
    private static String getMaterialNameForSide(String baseMaterialName, Side side) {
        MaterialData material = VoxelMaterialManager.getMaterial(baseMaterialName);
        if (material != null && material.hasSidedTextures()) {
            // Create side-specific material name
            String sideMaterialName = baseMaterialName + "_" + side.name().toLowerCase();

            // Register the side-specific material if it doesn't exist
            if (!VoxelMaterialManager.hasMaterial(sideMaterialName)) {
                String sideTexturePath = material.getTexturePath(side);
                VoxelMaterialManager.registerMaterial(sideMaterialName, sideTexturePath, material.diffuseColor(),
                        material.uvScale(), material.uvOffset());
            }

            return sideMaterialName;
        }
        return baseMaterialName;
    }

    private static Map<String, FaceMeshResult> meshFaceByMaterial(Chunk chunk, Side side) {
        Map<String, FaceMeshResult> materialResults = new HashMap<>();

        // Process each 2D slice based on face direction
        switch (side) {
        case TOP:
            processTopFaces(chunk, materialResults);
            break;
        case BOTTOM:
            processBottomFaces(chunk, materialResults);
            break;
        case FRONT:
            processFrontFaces(chunk, materialResults);
            break;
        case BACK:
            processBackFaces(chunk, materialResults);
            break;
        case LEFT:
            processLeftFaces(chunk, materialResults);
            break;
        case RIGHT:
            processRightFaces(chunk, materialResults);
            break;
        }

        return materialResults;
    }

    private static void processTopFaces(Chunk chunk, Map<String, FaceMeshResult> materialResults) {
        boolean[][][] processed = new boolean[World.CHUNK_SIZE][World.CHUNK_SIZE][World.CHUNK_SIZE];

        for (int y = 0; y < World.CHUNK_SIZE; y++) {
            for (int x = 0; x < World.CHUNK_SIZE; x++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    if (processed[x][y][z])
                        continue;

                    Block block = chunk.getBlock(x, y, z);
                    if (block == null || block == Blocks.AIR || !block.shouldRenderSide(chunk, x, y, z, Side.TOP)) {
                        processed[x][y][z] = true;
                        continue;
                    }

                    // Find greedy rectangle in X,Z plane for this specific Y level
                    int[] rect = findGreedyRectangle2D_XZ(chunk, x, z, processed, block, y, Side.TOP);
                    if (rect == null)
                        continue;

                    int width = rect[0];
                    int height = rect[1];
                    int endX = rect[2];
                    int endZ = rect[3];

                    // Validate that the entire rectangle should render this side
                    // Use lenient validation - only check if the original block should render
                    if (!block.shouldRenderSide(chunk, x, y, z, Side.TOP)) {
                        // Mark as processed to skip individual processing
                        for (int px = x; px <= endX && px < World.CHUNK_SIZE; px++) {
                            for (int pz = z; pz <= endZ && pz < World.CHUNK_SIZE; pz++) {
                                processed[px][y][pz] = true;
                            }
                        }
                        continue;
                    }

                    // Mark processed area for this specific Y level only
                    for (int px = x; px <= endX && px < World.CHUNK_SIZE; px++) {
                        for (int pz = z; pz <= endZ && pz < World.CHUNK_SIZE; pz++) {
                            processed[px][y][pz] = true;
                        }
                    }

                    // Create mesh for this material
                    String materialName = block.getMaterialName();
                    FaceMeshResult result = materialResults.computeIfAbsent(materialName, k -> new FaceMeshResult());
                    createQuad(result, x, y, z, width, height, Side.TOP);
                    result.faceCount += 2;
                    result.vertexCount += 4;
                }
            }
        }
    }

    private static void processBottomFaces(Chunk chunk, Map<String, FaceMeshResult> materialResults) {
        boolean[][][] processed = new boolean[World.CHUNK_SIZE][World.CHUNK_SIZE][World.CHUNK_SIZE];

        for (int y = 0; y < World.CHUNK_SIZE; y++) {
            for (int x = 0; x < World.CHUNK_SIZE; x++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    if (processed[x][y][z])
                        continue;

                    Block block = chunk.getBlock(x, y, z);
                    if (block == null || block == Blocks.AIR || !block.shouldRenderSide(chunk, x, y, z, Side.BOTTOM)) {
                        processed[x][y][z] = true;
                        continue;
                    }

                    // Find greedy rectangle in X,Z plane for this specific Y level
                    int[] rect = findGreedyRectangle2D_XZ(chunk, x, z, processed, block, y, Side.BOTTOM);
                    if (rect == null)
                        continue;

                    int width = rect[0];
                    int height = rect[1];
                    int endX = rect[2];
                    int endZ = rect[3];

                    // Validate that the entire rectangle should render this side
                    // Use lenient validation - only check if the original block should render
                    if (!block.shouldRenderSide(chunk, x, y, z, Side.BOTTOM)) {
                        // Mark as processed to skip individual processing
                        for (int px = x; px <= endX && px < World.CHUNK_SIZE; px++) {
                            for (int pz = z; pz <= endZ && pz < World.CHUNK_SIZE; pz++) {
                                processed[px][y][pz] = true;
                            }
                        }
                        continue;
                    }

                    // Mark processed area for this specific Y level only
                    for (int px = x; px <= endX && px < World.CHUNK_SIZE; px++) {
                        for (int pz = z; pz <= endZ && pz < World.CHUNK_SIZE; pz++) {
                            processed[px][y][pz] = true;
                        }
                    }

                    // Create mesh for this material
                    String materialName = block.getMaterialName();
                    FaceMeshResult result = materialResults.computeIfAbsent(materialName, k -> new FaceMeshResult());
                    createQuad(result, x, y, z, width, height, Side.BOTTOM);
                    result.faceCount += 2;
                    result.vertexCount += 4;
                }
            }
        }
    }

    private static void processFrontFaces(Chunk chunk, Map<String, FaceMeshResult> materialResults) {
        boolean[][][] processed = new boolean[World.CHUNK_SIZE][World.CHUNK_SIZE][World.CHUNK_SIZE];

        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int y = 0; y < World.CHUNK_SIZE; y++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    if (processed[x][y][z])
                        continue;

                    Block block = chunk.getBlock(x, y, z);
                    if (block == null || block == Blocks.AIR || !block.shouldRenderSide(chunk, x, y, z, Side.FRONT)) {
                        processed[x][y][z] = true;
                        continue;
                    }

                    // Find greedy rectangle in X,Y plane for this specific Z level
                    int[] rect = findGreedyRectangle2D_XY(chunk, x, y, processed, block, z, Side.FRONT);
                    if (rect == null)
                        continue;

                    int width = rect[0];
                    int height = rect[1];
                    int endX = rect[2];
                    int endY = rect[3];

                    // Validate that the entire rectangle should render this side
                    // Use lenient validation - only check if the original block should render
                    if (!block.shouldRenderSide(chunk, x, y, z, Side.FRONT)) {
                        // Mark as processed to skip individual processing
                        for (int px = x; px <= endX && px < World.CHUNK_SIZE; px++) {
                            for (int py = y; py <= endY && py < World.CHUNK_SIZE; py++) {
                                processed[px][py][z] = true;
                            }
                        }
                        continue;
                    }

                    // Mark processed area for this specific Z level only
                    for (int px = x; px <= endX && px < World.CHUNK_SIZE; px++) {
                        for (int py = y; py <= endY && py < World.CHUNK_SIZE; py++) {
                            processed[px][py][z] = true;
                        }
                    }

                    // Create mesh for this material
                    String materialName = block.getMaterialName();
                    FaceMeshResult result = materialResults.computeIfAbsent(materialName, k -> new FaceMeshResult());
                    createQuad(result, x, y, z, width, height, Side.FRONT);
                    result.faceCount += 2;
                    result.vertexCount += 4;
                }
            }
        }
    }

    private static void processBackFaces(Chunk chunk, Map<String, FaceMeshResult> materialResults) {
        boolean[][][] processed = new boolean[World.CHUNK_SIZE][World.CHUNK_SIZE][World.CHUNK_SIZE];

        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int y = 0; y < World.CHUNK_SIZE; y++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    if (processed[x][y][z])
                        continue;

                    Block block = chunk.getBlock(x, y, z);
                    if (block == null || block == Blocks.AIR || !block.shouldRenderSide(chunk, x, y, z, Side.BACK)) {
                        processed[x][y][z] = true;
                        continue;
                    }

                    // Find greedy rectangle in X,Y plane for this specific Z level
                    int[] rect = findGreedyRectangle2D_XY(chunk, x, y, processed, block, z, Side.BACK);
                    if (rect == null)
                        continue;

                    int width = rect[0];
                    int height = rect[1];
                    int endX = rect[2];
                    int endY = rect[3];

                    // Validate that the entire rectangle should render this side
                    // Use lenient validation - only check if the original block should render
                    if (!block.shouldRenderSide(chunk, x, y, z, Side.BACK)) {
                        // Mark as processed to skip individual processing
                        for (int px = x; px <= endX && px < World.CHUNK_SIZE; px++) {
                            for (int py = y; py <= endY && py < World.CHUNK_SIZE; py++) {
                                processed[px][py][z] = true;
                            }
                        }
                        continue;
                    }

                    // Mark processed area for this specific Z level only
                    for (int px = x; px <= endX && px < World.CHUNK_SIZE; px++) {
                        for (int py = y; py <= endY && py < World.CHUNK_SIZE; py++) {
                            processed[px][py][z] = true;
                        }
                    }

                    // Create mesh for this material
                    String materialName = block.getMaterialName();
                    FaceMeshResult result = materialResults.computeIfAbsent(materialName, k -> new FaceMeshResult());
                    createQuad(result, x, y, z, width, height, Side.BACK);
                    result.faceCount += 2;
                    result.vertexCount += 4;
                }
            }
        }
    }

    private static void processLeftFaces(Chunk chunk, Map<String, FaceMeshResult> materialResults) {
        boolean[][][] processed = new boolean[World.CHUNK_SIZE][World.CHUNK_SIZE][World.CHUNK_SIZE];

        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int y = 0; y < World.CHUNK_SIZE; y++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    if (processed[x][y][z])
                        continue;

                    Block block = chunk.getBlock(x, y, z);
                    if (block == null || block == Blocks.AIR || !block.shouldRenderSide(chunk, x, y, z, Side.LEFT)) {
                        processed[x][y][z] = true;
                        continue;
                    }

                    // Find greedy rectangle in Z,Y plane for this specific X level
                    int[] rect = findGreedyRectangle2D_ZY(chunk, z, y, processed, block, x, Side.LEFT);
                    if (rect == null)
                        continue;

                    int width = rect[0];
                    int height = rect[1];
                    int endZ = rect[2];
                    int endY = rect[3];

                    // Validate that the entire rectangle should render this side
                    // Use lenient validation - only check if the original block should render
                    if (!block.shouldRenderSide(chunk, x, y, z, Side.LEFT)) {
                        // Mark as processed to skip individual processing
                        for (int pz = z; pz <= endZ && pz < World.CHUNK_SIZE; pz++) {
                            for (int py = y; py <= endY && py < World.CHUNK_SIZE; py++) {
                                processed[x][py][pz] = true;
                            }
                        }
                        continue;
                    }

                    // Mark processed area for this specific X level only
                    for (int pz = z; pz <= endZ && pz < World.CHUNK_SIZE; pz++) {
                        for (int py = y; py <= endY && py < World.CHUNK_SIZE; py++) {
                            processed[x][py][pz] = true;
                        }
                    }

                    // Create mesh for this material
                    String materialName = block.getMaterialName();
                    FaceMeshResult result = materialResults.computeIfAbsent(materialName, k -> new FaceMeshResult());
                    createQuad(result, x, y, z, width, height, Side.LEFT);
                    result.faceCount += 2;
                    result.vertexCount += 4;
                }
            }
        }
    }

    private static void processRightFaces(Chunk chunk, Map<String, FaceMeshResult> materialResults) {
        boolean[][][] processed = new boolean[World.CHUNK_SIZE][World.CHUNK_SIZE][World.CHUNK_SIZE];

        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int y = 0; y < World.CHUNK_SIZE; y++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    if (processed[x][y][z])
                        continue;

                    Block block = chunk.getBlock(x, y, z);
                    if (block == null || block == Blocks.AIR || !block.shouldRenderSide(chunk, x, y, z, Side.RIGHT)) {
                        processed[x][y][z] = true;
                        continue;
                    }

                    // Find greedy rectangle in Z,Y plane for this specific X level
                    int[] rect = findGreedyRectangle2D_ZY(chunk, z, y, processed, block, x, Side.RIGHT);
                    if (rect == null)
                        continue;

                    int width = rect[0];
                    int height = rect[1];
                    int endZ = rect[2];
                    int endY = rect[3];

                    // Validate that the entire rectangle should render this side
                    // Use lenient validation - only check if the original block should render
                    if (!block.shouldRenderSide(chunk, x, y, z, Side.RIGHT)) {
                        // Mark as processed to skip individual processing
                        for (int pz = z; pz <= endZ && pz < World.CHUNK_SIZE; pz++) {
                            for (int py = y; py <= endY && py < World.CHUNK_SIZE; py++) {
                                processed[x][py][pz] = true;
                            }
                        }
                        continue;
                    }

                    // Mark processed area for this specific X level only
                    for (int pz = z; pz <= endZ && pz < World.CHUNK_SIZE; pz++) {
                        for (int py = y; py <= endY && py < World.CHUNK_SIZE; py++) {
                            processed[x][py][pz] = true;
                        }
                    }

                    // Create mesh for this material
                    String materialName = block.getMaterialName();
                    FaceMeshResult result = materialResults.computeIfAbsent(materialName, k -> new FaceMeshResult());
                    createQuad(result, x, y, z, width, height, Side.RIGHT);
                    result.faceCount += 2;
                    result.vertexCount += 4;
                }
            }
        }
    }

    private static int[] findGreedyRectangle2D_XZ(Chunk chunk, int startX, int startZ, boolean[][][] processed,
            Block blockType, int y, Side side) {
        int width = 1;
        int height = 1;

        // Expand width in X direction
        while (canExpand2D_XZ(chunk, startX + width, startZ, y, processed, blockType, side)) {
            width++;
        }

        // Expand height in Z direction
        while (canExpand2D_XZ(chunk, startX, startZ + height, y, processed, blockType, side)) {
            height++;
        }

        // Check full rectangle validity
        for (int w = 1; w < width; w++) {
            for (int h = 1; h < height; h++) {
                if (!canExpand2D_XZ(chunk, startX + w, startZ + h, y, processed, blockType, side)) {
                    if (w > 1)
                        width = w;
                    if (h > 1)
                        height = h;
                    break;
                }
            }
        }

        return new int[] { width, height, startX + width - 1, startZ + height - 1 };
    }

    private static int[] findGreedyRectangle2D_XY(Chunk chunk, int startX, int startY, boolean[][][] processed,
            Block blockType, int z, Side side) {
        int width = 1;
        int height = 1;

        // Expand width in X direction
        while (canExpand2D_XY(chunk, startX + width, startY, z, processed, blockType, side)) {
            width++;
        }

        // Expand height in Y direction
        while (canExpand2D_XY(chunk, startX, startY + height, z, processed, blockType, side)) {
            height++;
        }

        // Check full rectangle validity
        for (int w = 1; w < width; w++) {
            for (int h = 1; h < height; h++) {
                if (!canExpand2D_XY(chunk, startX + w, startY + h, z, processed, blockType, side)) {
                    if (w > 1)
                        width = w;
                    if (h > 1)
                        height = h;
                    break;
                }
            }
        }

        return new int[] { width, height, startX + width - 1, startY + height - 1 };
    }

    private static int[] findGreedyRectangle2D_ZY(Chunk chunk, int startZ, int startY, boolean[][][] processed,
            Block blockType, int x, Side side) {
        int width = 1;
        int height = 1;

        // Expand width in Z direction
        while (canExpand2D_ZY(chunk, startZ + width, startY, x, processed, blockType, side)) {
            width++;
        }

        // Expand height in Y direction
        while (canExpand2D_ZY(chunk, startZ, startY + height, x, processed, blockType, side)) {
            height++;
        }

        // Check full rectangle validity
        for (int w = 1; w < width; w++) {
            for (int h = 1; h < height; h++) {
                if (!canExpand2D_ZY(chunk, startZ + w, startY + h, x, processed, blockType, side)) {
                    if (w > 1)
                        width = w;
                    if (h > 1)
                        height = h;
                    break;
                }
            }
        }

        return new int[] { width, height, startZ + width - 1, startY + height - 1 };
    }

    private static boolean canExpand2D_XZ(Chunk chunk, int x, int z, int y, boolean[][][] processed, Block blockType,
            Side side) {
        if (x < 0 || x >= World.CHUNK_SIZE || z < 0 || z >= World.CHUNK_SIZE) {
            return false;
        }

        if (processed[x][y][z])
            return false;

        Block otherBlock = chunk.getBlock(x, y, z);
        // Only check if blocks are the same type for greedy meshing
        // Visibility check is done separately for the entire rectangle
        return otherBlock != null && otherBlock == blockType;
    }

    private static boolean canExpand2D_XY(Chunk chunk, int x, int y, int z, boolean[][][] processed, Block blockType,
            Side side) {
        if (x < 0 || x >= World.CHUNK_SIZE || y < 0 || y >= World.CHUNK_SIZE) {
            return false;
        }

        if (processed[x][y][z])
            return false;

        Block otherBlock = chunk.getBlock(x, y, z);
        // Only check if blocks are the same type for greedy meshing
        // Visibility check is done separately for the entire rectangle
        return otherBlock != null && otherBlock == blockType;
    }

    private static boolean canExpand2D_ZY(Chunk chunk, int z, int y, int x, boolean[][][] processed, Block blockType,
            Side side) {
        if (z < 0 || z >= World.CHUNK_SIZE || y < 0 || y >= World.CHUNK_SIZE) {
            return false;
        }

        if (processed[x][y][z])
            return false;

        Block otherBlock = chunk.getBlock(x, y, z);
        // Only check if blocks are the same type for greedy meshing
        // Visibility check is done separately for the entire rectangle
        return otherBlock != null && otherBlock == blockType;
    }

    private static void createQuad(FaceMeshResult result, int x, int y, int z, int width, int height, Side side) {
        int baseIndex = result.vertices.size() / 3;

        // Calculate the four corners of the quad with proper world coordinates
        Vector3f[] corners = calculateQuadCorners(x, y, z, width, height, side);

        // Add vertices and TILED texture coordinates
        for (int i = 0; i < 4; i++) {
            Vector3f corner = corners[i];
            result.vertices.add(corner.x);
            result.vertices.add(corner.y);
            result.vertices.add(corner.z);

            // Use tiled texture coordinates - each block gets a full texture
            float u, v;

            // For side faces, flip V coordinate to fix upside-down textures
            boolean isSideFace = (side == Side.FRONT || side == Side.BACK || side == Side.LEFT || side == Side.RIGHT);

            switch (i) {
            case 0: // Bottom-left corner
                u = 0.0f;
                v = isSideFace ? (float) height : 0.0f; // Flip V for side faces
                break;
            case 1: // Bottom-right corner
                u = (float) width; // Tile across width
                v = isSideFace ? (float) height : 0.0f; // Flip V for side faces
                break;
            case 2: // Top-right corner
                u = (float) width; // Tile across width
                v = isSideFace ? 0.0f : (float) height; // Flip V for side faces
                break;
            case 3: // Top-left corner
                u = 0.0f;
                v = isSideFace ? 0.0f : (float) height; // Flip V for side faces
                break;
            default:
                u = 0.0f;
                v = 0.0f;
                break;
            }
            result.texCoords.add(u);
            result.texCoords.add(v);
        }

        // Add indices for two triangles
        result.indices.add(baseIndex);
        result.indices.add(baseIndex + 1);
        result.indices.add(baseIndex + 2);

        result.indices.add(baseIndex);
        result.indices.add(baseIndex + 2);
        result.indices.add(baseIndex + 3);
    }

    private static Vector3f[] calculateQuadCorners(int x, int y, int z, int width, int height, Side side) {
        Vector3f[] corners = new Vector3f[4];

        switch (side) {
        case TOP:
            corners[0] = new Vector3f(x, y + 1, z);
            corners[1] = new Vector3f(x + width, y + 1, z);
            corners[2] = new Vector3f(x + width, y + 1, z + height);
            corners[3] = new Vector3f(x, y + 1, z + height);
            break;
        case BOTTOM:
            corners[0] = new Vector3f(x, y, z);
            corners[1] = new Vector3f(x, y, z + height);
            corners[2] = new Vector3f(x + width, y, z + height);
            corners[3] = new Vector3f(x + width, y, z);
            break;
        case FRONT:
            corners[0] = new Vector3f(x, y, z + 1);
            corners[1] = new Vector3f(x + width, y, z + 1);
            corners[2] = new Vector3f(x + width, y + height, z + 1);
            corners[3] = new Vector3f(x, y + height, z + 1);
            break;
        case BACK:
            corners[0] = new Vector3f(x + width, y, z);
            corners[1] = new Vector3f(x, y, z);
            corners[2] = new Vector3f(x, y + height, z);
            corners[3] = new Vector3f(x + width, y + height, z);
            break;
        case RIGHT:
            corners[0] = new Vector3f(x + 1, y, z + width);
            corners[1] = new Vector3f(x + 1, y, z);
            corners[2] = new Vector3f(x + 1, y + height, z);
            corners[3] = new Vector3f(x + 1, y + height, z + width);
            break;
        case LEFT:
            corners[0] = new Vector3f(x, y, z);
            corners[1] = new Vector3f(x, y, z + width);
            corners[2] = new Vector3f(x, y + height, z + width);
            corners[3] = new Vector3f(x, y + height, z);
            break;
        default:
            // Default to front face
            corners[0] = new Vector3f(x, y, z + 1);
            corners[1] = new Vector3f(x + width, y, z + 1);
            corners[2] = new Vector3f(x + width, y + height, z + 1);
            corners[3] = new Vector3f(x, y + height, z + 1);
            break;
        }

        return corners;
    }
}
