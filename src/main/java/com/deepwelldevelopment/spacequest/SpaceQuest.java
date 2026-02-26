package com.deepwelldevelopment.spacequest;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.tinylog.Logger;

import com.deepwelldevelopment.spacequest.engine.Engine;
import com.deepwelldevelopment.spacequest.engine.EngineContext;
import com.deepwelldevelopment.spacequest.engine.InitData;
import com.deepwelldevelopment.spacequest.engine.model.MaterialData;
import com.deepwelldevelopment.spacequest.engine.model.ModelData;
import com.deepwelldevelopment.spacequest.engine.model.ModelLoader;
import com.deepwelldevelopment.spacequest.engine.scene.Entity;
import com.deepwelldevelopment.spacequest.engine.scene.Scene;

public class SpaceQuest {

    private final Vector3f rotatingAngle = new Vector3f(1, 1, 1);
    private float angle = 0.0f;
    private Entity cubeEntity;

    public InitData init(EngineContext engineContext) {
        Scene scene = engineContext.scene();
        List<ModelData> models = new ArrayList<>();

        ModelData cubeModel = ModelLoader.loadModel("resources/models/cube/cube.json");
        models.add(cubeModel);
        cubeEntity = new Entity("CubeEntity", cubeModel.id(), new Vector3f(0.0f, 0.0f, -2.0f));
        scene.addEntity(cubeEntity);

        List<MaterialData> materials = new ArrayList<>(
                ModelLoader.loadMaterials("resources/models/cube/cube_mat.json"));

        return new InitData(models, materials);
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
