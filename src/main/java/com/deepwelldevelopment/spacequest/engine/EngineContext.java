package com.deepwelldevelopment.spacequest.engine;

import com.deepwelldevelopment.spacequest.engine.scene.Scene;
import com.deepwelldevelopment.spacequest.engine.window.Window;

public record EngineContext(Engine engine, Window window, Scene scene) {

    public void cleanup() {
        window.cleanup();
    }

}
