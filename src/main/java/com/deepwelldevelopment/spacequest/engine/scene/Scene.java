package com.deepwelldevelopment.spacequest.engine.scene;

import com.deepwelldevelopment.spacequest.engine.EngineConfig;
import com.deepwelldevelopment.spacequest.engine.window.Window;

import java.util.ArrayList;
import java.util.List;

public class Scene {

    private final List<Entity> entities;
    private final Projection projection;
    private final Camera camera;

    public Scene(Window window) {
        entities = new ArrayList<>();
        var engCfg = EngineConfig.getInstance();
        projection = new Projection(engCfg.getFov(), engCfg.getZNear(), engCfg.getZFar(), window.getWidth(),
                window.getHeight());
        camera = new Camera();
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public Projection getProjection() {
        return projection;
    }

    public Camera getCamera() {
        return camera;
    }

    public void removeAllEntities() {
        entities.clear();
    }

    public void removeEntity(Entity entity) {
        entities.removeIf(e -> e.getId().equals(entity.getId()));
    }

    public void removeEntity(String entityId) {
        entities.removeIf(e -> e.getId().equals(entityId));
    }
}
