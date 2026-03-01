package com.deepwelldevelopment.spacequest.engine.model;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import com.deepwelldevelopment.spacequest.engine.graph.vk.VulkanUtils;

public class ProgrammaticModel {
    private final String id;
    private final List<ProgrammaticMesh> meshes;
    private final ByteArrayOutputStream vertexData;
    private final ByteArrayOutputStream indexData;
    private final DataOutputStream vertexOutput;
    private final DataOutputStream indexOutput;

    public ProgrammaticModel(String id) {
        this.id = id;
        this.meshes = new ArrayList<>();
        this.vertexData = new ByteArrayOutputStream();
        this.indexData = new ByteArrayOutputStream();
        this.vertexOutput = new DataOutputStream(vertexData);
        this.indexOutput = new DataOutputStream(indexData);
    }

    public ProgrammaticMesh addMesh(String meshId, String materialId) {
        ProgrammaticMesh mesh = new ProgrammaticMesh(meshId, materialId, this);
        meshes.add(mesh);
        return mesh;
    }

    public ModelData toModelData() {
        List<MeshData> meshDataList = new ArrayList<>();
        for (ProgrammaticMesh mesh : meshes) {
            meshDataList.add(mesh.toMeshData());
        }

        // Create in-memory data sources
        String vertexPath = "memory://" + id + ".vtx";
        String indexPath = "memory://" + id + ".idx";

        // Store the data in a static registry for ModelsCache to access
        ModelDataRegistry.registerModel(id, vertexData.toByteArray(),
                indexData.toByteArray());

        return new ModelData(id, meshDataList, vertexPath, indexPath);
    }

    void writeVertexData(float x, float y, float z, float u, float v) throws IOException {
        vertexOutput.writeFloat(x);
        vertexOutput.writeFloat(y);
        vertexOutput.writeFloat(z);
        vertexOutput.writeFloat(u);
        vertexOutput.writeFloat(v);
    }

    void writeIndexData(int index) throws IOException {
        indexOutput.writeInt(index);
    }

    public String getId() {
        return id;
    }

    public static class ProgrammaticMesh {
        private final String id;
        private final String materialId;
        private final ProgrammaticModel model;
        public final List<Float> vertices;
        public final List<Float> texCoords;
        public final List<Integer> indices;
        private int vertexOffset;
        private int indexOffset;

        public ProgrammaticMesh(String id, String materialId, ProgrammaticModel model) {
            this.id = id;
            this.materialId = materialId;
            this.model = model;
            this.vertices = new ArrayList<>();
            this.texCoords = new ArrayList<>();
            this.indices = new ArrayList<>();
            this.vertexOffset = 0;
            this.indexOffset = 0;
        }

        public ProgrammaticMesh addVertex(float x, float y, float z, float u, float v) {
            vertices.add(x);
            vertices.add(y);
            vertices.add(z);
            texCoords.add(u);
            texCoords.add(v);
            return this;
        }

        public ProgrammaticMesh addVertex(Vector3f position, float u, float v) {
            return addVertex(position.x, position.y, position.z, u, v);
        }

        public ProgrammaticMesh addTriangle(int i0, int i1, int i2) {
            indices.add(i0);
            indices.add(i1);
            indices.add(i2);
            return this;
        }

        public ProgrammaticMesh addQuad(int i0, int i1, int i2, int i3) {
            indices.add(i0);
            indices.add(i1);
            indices.add(i2);
            indices.add(i2);
            indices.add(i3);
            indices.add(i0);
            return this;
        }

        public ProgrammaticMesh finalizeMesh() {
            try {
                // Calculate offsets based on current data size
                this.vertexOffset = model.vertexData.size();
                this.indexOffset = model.indexData.size();

                // Write vertex data
                for (int i = 0; i < vertices.size(); i += 3) {
                    int texIndex = (i / 3) * 2;
                    model.writeVertexData(
                            vertices.get(i), vertices.get(i + 1), vertices.get(i + 2),
                            texCoords.get(texIndex), texCoords.get(texIndex + 1));
                }

                // Write index data
                for (int index : indices) {
                    model.writeIndexData(index);
                }

            } catch (IOException e) {
                throw new RuntimeException("Failed to finalize mesh", e);
            }
            return this;
        }

        public MeshData toMeshData() {
            int vertexSize = vertices.size() * VulkanUtils.FLOAT_SIZE + texCoords.size() * VulkanUtils.FLOAT_SIZE;
            int indexSize = indices.size() * VulkanUtils.INT_SIZE;

            return new MeshData(id, materialId, vertexOffset, vertexSize, indexOffset, indexSize);
        }

        public String getId() {
            return id;
        }

        public String getMaterialId() {
            return materialId;
        }
    }
}
