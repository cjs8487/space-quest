package com.deepwelldevelopment.spacequest;

import java.util.ArrayList;
import java.util.List;

import org.tinylog.Logger;

import com.deepwelldevelopment.spacequest.engine.Engine;
import com.deepwelldevelopment.spacequest.engine.EngineContext;
import com.deepwelldevelopment.spacequest.engine.model.MeshData;
import com.deepwelldevelopment.spacequest.engine.model.ModelData;

public class SpaceQuest {

    public List<ModelData> init(EngineContext engineContext) {
        var modelId = "TriangleModel";
        MeshData meshData = new MeshData("triangle-mesh", new float[] {
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.0f, 0.5f, 0.0f
        }, new int[] { 0, 1, 2 });
        List<MeshData> meshDataList = new ArrayList<>();
        meshDataList.add(meshData);
        ModelData modelData = new ModelData(modelId, meshDataList);
        List<ModelData> models = new ArrayList<>();
        models.add(modelData);

        return models;
    }

    public void input(EngineContext engineContext, long deltaTime) {
    }

    public void update(EngineContext engineContext, long deltaTime) {
    }

    public void cleanup() {
    }

    public static void main(String[] args) {
        Logger.info("Starting Space Quest...");
        var engine = new Engine("Space Quest", new SpaceQuest());
        Logger.info("Started Space Quest");
        engine.run();
    }
}
