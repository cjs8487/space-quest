package com.deepwelldevelopment.spacequest.engine.model;

import com.deepwelldevelopment.spacequest.engine.model.ProgrammaticModel.ProgrammaticMesh;

public class MeshBuilder {

   public static ProgrammaticModel createCube(String modelId, String materialId, float size) {
      return createCube(modelId, materialId, size, 0, 1, 0, 1);
   }

   public static ProgrammaticModel createCube(String modelId, String materialId, float size,
         float uMin, float uMax, float vMin, float vMax) {
      ProgrammaticModel model = new ProgrammaticModel(modelId);
      ProgrammaticMesh mesh = model.addMesh(modelId + "_mesh", materialId);

      float halfSize = size / 2.0f;

      // Define vertices for each face of the cube
      // Front face (Z+)
      int frontStart = mesh.vertices.size() / 3;
      mesh.addVertex(-halfSize, -halfSize, halfSize, uMin, vMax) // Bottom-left
            .addVertex(halfSize, -halfSize, halfSize, uMax, vMax) // Bottom-right
            .addVertex(halfSize, halfSize, halfSize, uMax, vMin) // Top-right
            .addVertex(-halfSize, halfSize, halfSize, uMin, vMin); // Top-left

      // Back face (Z-)
      int backStart = mesh.vertices.size() / 3;
      mesh.addVertex(halfSize, -halfSize, -halfSize, uMin, vMax) // Bottom-left
            .addVertex(-halfSize, -halfSize, -halfSize, uMax, vMax) // Bottom-right
            .addVertex(-halfSize, halfSize, -halfSize, uMax, vMin) // Top-right
            .addVertex(halfSize, halfSize, -halfSize, uMin, vMin); // Top-left

      // Top face (Y+)
      int topStart = mesh.vertices.size() / 3;
      mesh.addVertex(-halfSize, halfSize, halfSize, uMin, vMin) // Top-left
            .addVertex(halfSize, halfSize, halfSize, uMax, vMin) // Top-right
            .addVertex(halfSize, halfSize, -halfSize, uMax, vMax) // Bottom-right
            .addVertex(-halfSize, halfSize, -halfSize, uMin, vMax); // Bottom-left

      // Bottom face (Y-)
      int bottomStart = mesh.vertices.size() / 3;
      mesh.addVertex(-halfSize, -halfSize, -halfSize, uMin, vMax) // Bottom-left
            .addVertex(halfSize, -halfSize, -halfSize, uMax, vMax) // Bottom-right
            .addVertex(halfSize, -halfSize, halfSize, uMax, vMin) // Top-right
            .addVertex(-halfSize, -halfSize, halfSize, uMin, vMin); // Top-left

      // Right face (X+)
      int rightStart = mesh.vertices.size() / 3;
      mesh.addVertex(halfSize, -halfSize, halfSize, uMin, vMax) // Bottom-left
            .addVertex(halfSize, -halfSize, -halfSize, uMax, vMax) // Bottom-right
            .addVertex(halfSize, halfSize, -halfSize, uMax, vMin) // Top-right
            .addVertex(halfSize, halfSize, halfSize, uMin, vMin); // Top-left

      // Left face (X-)
      int leftStart = mesh.vertices.size() / 3;
      mesh.addVertex(-halfSize, -halfSize, -halfSize, uMin, vMax) // Bottom-left
            .addVertex(-halfSize, -halfSize, halfSize, uMax, vMax) // Bottom-right
            .addVertex(-halfSize, halfSize, halfSize, uMax, vMin) // Top-right
            .addVertex(-halfSize, halfSize, -halfSize, uMin, vMin); // Top-left

      // Add indices for each face (2 triangles per face)
      // Front face
      mesh.addQuad(frontStart, frontStart + 1, frontStart + 2, frontStart + 3);
      // Back face
      mesh.addQuad(backStart, backStart + 1, backStart + 2, backStart + 3);
      // Top face
      mesh.addQuad(topStart, topStart + 1, topStart + 2, topStart + 3);
      // Bottom face
      mesh.addQuad(bottomStart, bottomStart + 1, bottomStart + 2, bottomStart + 3);
      // Right face
      mesh.addQuad(rightStart, rightStart + 1, rightStart + 2, rightStart + 3);
      // Left face
      mesh.addQuad(leftStart, leftStart + 1, leftStart + 2, leftStart + 3);

      mesh.finalizeMesh();
      return model;
   }

   public static ProgrammaticModel createPlane(String modelId, String materialId,
         float width, float height) {
      return createPlane(modelId, materialId, width, height, 0, 1, 0, 1);
   }

   public static ProgrammaticModel createPlane(String modelId, String materialId,
         float width, float height, float uMin, float uMax, float vMin, float vMax) {
      ProgrammaticModel model = new ProgrammaticModel(modelId);
      ProgrammaticMesh mesh = model.addMesh(modelId + "_mesh", materialId);

      float halfWidth = width / 2.0f;
      float halfHeight = height / 2.0f;

      // Define vertices for the plane (Y=0)
      mesh.addVertex(-halfWidth, 0.0f, halfHeight, uMin, vMin) // Top-left
            .addVertex(halfWidth, 0.0f, halfHeight, uMax, vMin) // Top-right
            .addVertex(halfWidth, 0.0f, -halfHeight, uMax, vMax) // Bottom-right
            .addVertex(-halfWidth, 0.0f, -halfHeight, uMin, vMax); // Bottom-left

      // Add indices for the plane (2 triangles)
      mesh.addQuad(0, 1, 2, 3);

      mesh.finalizeMesh();
      return model;
   }

   public static ProgrammaticModel createCustomBox(String modelId, String materialId,
         float width, float height, float depth) {
      return createCustomBox(modelId, materialId, width, height, depth, 0, 1, 0, 1);
   }

   public static ProgrammaticModel createCustomBox(String modelId, String materialId,
         float width, float height, float depth,
         float uMin, float uMax, float vMin, float vMax) {
      ProgrammaticModel model = new ProgrammaticModel(modelId);
      ProgrammaticMesh mesh = model.addMesh(modelId + "_mesh", materialId);

      float halfWidth = width / 2.0f;
      float halfHeight = height / 2.0f;
      float halfDepth = depth / 2.0f;

      // Front face (Z+)
      int frontStart = mesh.vertices.size() / 3;
      mesh.addVertex(-halfWidth, -halfHeight, halfDepth, uMin, vMax)
            .addVertex(halfWidth, -halfHeight, halfDepth, uMax, vMax)
            .addVertex(halfWidth, halfHeight, halfDepth, uMax, vMin)
            .addVertex(-halfWidth, halfHeight, halfDepth, uMin, vMin);

      // Back face (Z-)
      int backStart = mesh.vertices.size() / 3;
      mesh.addVertex(halfWidth, -halfHeight, -halfDepth, uMin, vMax)
            .addVertex(-halfWidth, -halfHeight, -halfDepth, uMax, vMax)
            .addVertex(-halfWidth, halfHeight, -halfDepth, uMax, vMin)
            .addVertex(halfWidth, halfHeight, -halfDepth, uMin, vMin);

      // Top face (Y+)
      int topStart = mesh.vertices.size() / 3;
      mesh.addVertex(-halfWidth, halfHeight, halfDepth, uMin, vMin)
            .addVertex(halfWidth, halfHeight, halfDepth, uMax, vMin)
            .addVertex(halfWidth, halfHeight, -halfDepth, uMax, vMax)
            .addVertex(-halfWidth, halfHeight, -halfDepth, uMin, vMax);

      // Bottom face (Y-)
      int bottomStart = mesh.vertices.size() / 3;
      mesh.addVertex(-halfWidth, -halfHeight, -halfDepth, uMin, vMax)
            .addVertex(halfWidth, -halfHeight, -halfDepth, uMax, vMax)
            .addVertex(halfWidth, -halfHeight, halfDepth, uMax, vMin)
            .addVertex(-halfWidth, -halfHeight, halfDepth, uMin, vMin);

      // Right face (X+)
      int rightStart = mesh.vertices.size() / 3;
      mesh.addVertex(halfWidth, -halfHeight, halfDepth, uMin, vMax)
            .addVertex(halfWidth, -halfHeight, -halfDepth, uMax, vMax)
            .addVertex(halfWidth, halfHeight, -halfDepth, uMax, vMin)
            .addVertex(halfWidth, halfHeight, halfDepth, uMin, vMin);

      // Left face (X-)
      int leftStart = mesh.vertices.size() / 3;
      mesh.addVertex(-halfWidth, -halfHeight, -halfDepth, uMin, vMax)
            .addVertex(-halfWidth, -halfHeight, halfDepth, uMax, vMax)
            .addVertex(-halfWidth, halfHeight, halfDepth, uMax, vMin)
            .addVertex(-halfWidth, halfHeight, -halfDepth, uMin, vMin);

      // Add indices for each face
      mesh.addQuad(frontStart, frontStart + 1, frontStart + 2, frontStart + 3);
      mesh.addQuad(backStart, backStart + 1, backStart + 2, backStart + 3);
      mesh.addQuad(topStart, topStart + 1, topStart + 2, topStart + 3);
      mesh.addQuad(bottomStart, bottomStart + 1, bottomStart + 2, bottomStart + 3);
      mesh.addQuad(rightStart, rightStart + 1, rightStart + 2, rightStart + 3);
      mesh.addQuad(leftStart, leftStart + 1, leftStart + 2, leftStart + 3);

      mesh.finalizeMesh();
      return model;
   }
}
