package com.deepwelldevelopment.spacequest.engine.model;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ModelBinData {

    private final String indexFilePath;
    private final DataOutputStream indexOutput;
    private final String vertexFilePath;
    private final DataOutputStream vertexOutput;
    private int indexOffset;
    private int vertexOffset;

    public ModelBinData(String modelPath) throws FileNotFoundException {
        vertexFilePath = modelPath.substring(0, modelPath.lastIndexOf('.')) + ".vtx";
        vertexOutput = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(vertexFilePath)));
        indexFilePath = modelPath.substring(0, modelPath.lastIndexOf('.')) + ".idx";
        indexOutput = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(indexFilePath)));
    }

    public void close() throws IOException {
        vertexOutput.close();
        indexOutput.close();
    }

    public String getIndexFilePath() {
        return indexFilePath;
    }

    public int getIndexOffset() {
        return indexOffset;
    }

    public DataOutputStream getIndexOutput() {
        return indexOutput;
    }

    public String getVertexFilePath() {
        return vertexFilePath;
    }

    public int getVertexOffset() {
        return vertexOffset;
    }

    public DataOutputStream getVertexOutput() {
        return vertexOutput;
    }

    public void incIndexOffset(int inc) {
        indexOffset += inc;
    }

    public void incVertexOffset(int inc) {
        vertexOffset += inc;
    }
}
