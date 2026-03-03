package com.deepwelldevelopment.spacequest.engine;

import com.deepwelldevelopment.spacequest.SpaceQuest;
import com.deepwelldevelopment.spacequest.engine.graphics.Renderer;
import com.deepwelldevelopment.spacequest.engine.scene.Scene;
import com.deepwelldevelopment.spacequest.engine.window.Window;

public class Engine {

    private final SpaceQuest spaceQuest;
    private final EngineContext engineContext;
    private final Renderer renderer;

    // FPS and UPS tracking
    private int fps = 0;
    private int ups = 0;
    private long lastFpsTime = 0;
    private long lastUpsTime = 0;
    private int frameCount = 0;
    private int updateCount = 0;

    public Engine(String windowTitle, SpaceQuest spaceQuest) {
        this.spaceQuest = spaceQuest;
        var window = new Window(windowTitle);
        this.engineContext = new EngineContext(this, window, new Scene(window));
        this.renderer = new Renderer(engineContext);
        var world = spaceQuest.init(engineContext, renderer);
        renderer.init(this.engineContext, world);
    }

    private void cleanup() {
        spaceQuest.cleanup();
    }

    public int getFps() {
        return fps;
    }

    public int getUps() {
        return ups;
    }

    public void run() {
        var cfg = EngineConfig.getInstance();
        long initialTime = System.currentTimeMillis();
        float timeU = 1000.0f / cfg.getUps();
        double deltaUpdate = 0.0;

        long updateTime = initialTime;
        lastFpsTime = initialTime;
        lastUpsTime = initialTime;
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

                // Track UPS
                updateCount++;
                if (now - lastUpsTime >= 1000) {
                    ups = updateCount;
                    updateCount = 0;
                    lastUpsTime = now;
                }
            }

            renderer.render(engineContext);

            // Track FPS
            frameCount++;
            if (now - lastFpsTime >= 1000) {
                fps = frameCount;
                frameCount = 0;
                lastFpsTime = now;
            }

            initialTime = now;
        }

        cleanup();
    }
}
