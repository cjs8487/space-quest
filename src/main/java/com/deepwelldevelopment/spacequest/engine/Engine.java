package com.deepwelldevelopment.spacequest.engine;

import com.deepwelldevelopment.spacequest.SpaceQuest;
import com.deepwelldevelopment.spacequest.engine.graph.Renderer;
import com.deepwelldevelopment.spacequest.engine.scene.Scene;
import com.deepwelldevelopment.spacequest.engine.window.Window;

public class Engine {

    private final SpaceQuest spaceQuest;
    private final EngineContext engineContext;
    private final Renderer renderer;

    public Engine(String windowTitle, SpaceQuest spaceQuest) {
        this.spaceQuest = spaceQuest;
        var window = new Window(windowTitle);
        this.engineContext = new EngineContext(window, new Scene(window));
        this.renderer = new Renderer(engineContext);
        var world = spaceQuest.init(engineContext, renderer);
        renderer.init(this.engineContext, world);
    }

    private void cleanup() {
        spaceQuest.cleanup();
    }

    public void run() {
        var cfg = EngineConfig.getInstance();
        long initialTime = System.currentTimeMillis();
        float timeU = 1000.0f / cfg.getUps();
        double deltaUpdate = 0.0;

        long updateTime = initialTime;
        Window window = engineContext.window();
        while (!window.shouldClose()) {
            long now = System.currentTimeMillis();
            deltaUpdate += (now - updateTime) / timeU;
            updateTime = now;

            window.pollEvents();
            spaceQuest.input(engineContext, now - initialTime);
            window.resetInput();

            if (deltaUpdate >= 1.0) {
                long diffTimeMillis = now - updateTime;
                spaceQuest.update(engineContext, diffTimeMillis);
                updateTime = now;
                deltaUpdate--;
            }

            renderer.render(engineContext);

            initialTime = now;
        }

        cleanup();
    }
}
