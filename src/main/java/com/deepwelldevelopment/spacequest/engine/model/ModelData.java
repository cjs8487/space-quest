package com.deepwelldevelopment.spacequest.engine.model;

import java.util.List;

public record ModelData(String id, List<MeshData> meshes, String vertexPath, String indexPath) {

}
