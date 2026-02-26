package com.deepwelldevelopment.spacequest;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.tinylog.Logger;

import com.deepwelldevelopment.spacequest.engine.Engine;
import com.deepwelldevelopment.spacequest.engine.EngineContext;
import com.deepwelldevelopment.spacequest.engine.model.MeshData;
import com.deepwelldevelopment.spacequest.engine.model.ModelData;
import com.deepwelldevelopment.spacequest.engine.scene.Entity;

public class SpaceQuest {

    private final Vector3f rotatingAngle = new Vector3f(1, 1, 1);
    private float angle = 0.0f;
    private Entity cubeEntity;

    public List<ModelData> init(EngineContext engineContext) {
        float[] positions = new float[] {
                -0.5f, 0.5f, 0.5f,
                -0.5f, -0.5f, 0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f
        };
        float[] texCoords = new float[] {
                0.0f, 0.0f,
                0.5f, 0.0f,
                1.0f, 0.0f,
                1.0f, 0.5f,
                1.0f, 1.0f,
                0.5f, 1.0f,
                0.0f, 1.0f,
                0.0f, 0.5f
        };
        int[] indices = new int[] {
                // front
                0, 1, 3, 3, 1, 2,
                // top
                4, 0, 3, 5, 4, 3,
                // right
                3, 2, 7, 5, 3, 7,
                // left
                6, 1, 0, 6, 0, 4,
                // bottom
                2, 1, 6, 2, 6, 7,
                // back
                7, 6, 4, 7, 4, 5
        };
        var modelId = "CubeModel";
        MeshData meshData = new MeshData("cube-mesh", positions, texCoords, indices);
        List<MeshData> meshDataList = new ArrayList<>();
        meshDataList.add(meshData);
        ModelData modelData = new ModelData(modelId, meshDataList);
        List<ModelData> models = new ArrayList<>();
        models.add(modelData);

        cubeEntity = new Entity("CubeEntity", modelId, new Vector3f(0.0f, 0.0f, -2.0f));
        engineContext.scene().addEntity(cubeEntity);

        return models;
    }

    public void input(EngineContext engineContext, long deltaTime) {
    }

    public void update(EngineContext engineContext, long deltaTime) {
        angle += 1.0f;
        if (angle >= 360.0f) {
            angle = angle - 360.0f;
        }
        cubeEntity.getRotation().identity().rotateAxis((float) Math.toRadians(angle), rotatingAngle);
        cubeEntity.updateModelMatrix();
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
