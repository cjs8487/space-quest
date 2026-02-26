package com.deepwelldevelopment.spacequest.engine.scene;

import com.deepwelldevelopment.spacequest.engine.EngineConfig;
import com.deepwelldevelopment.spacequest.engine.window.Window;

import java.util.ArrayList;
import java.util.List;

public class Scene {

    private final List<Entity> entities;
    private final Projection projection;

    public Scene(Window window) {
        entities = new ArrayList<>();
        var engCfg = EngineConfig.getInstance();
        projection = new Projection(engCfg.getFov(), engCfg.getZNear(), engCfg.getZFar(), window.getWidth(),
                window.getHeight());
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

    public void removeAllEntities() {
        entities.clear();
    }

    public void removeEntity(Entity entity) {
        entities.removeIf(e -> e.getId().equals(entity.getId()));
    }
}
