package com.deepwelldevelopment.spacequest;

import org.tinylog.Logger;

import com.deepwelldevelopment.spacequest.engine.Engine;
import com.deepwelldevelopment.spacequest.engine.EngineContext;

public class SpaceQuest {

    public void init(EngineContext engineContext) {
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
