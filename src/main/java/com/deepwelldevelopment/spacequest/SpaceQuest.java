package com.deepwelldevelopment.spacequest;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import org.joml.Vector2f;
import org.tinylog.Logger;

import com.deepwelldevelopment.spacequest.engine.Engine;
import com.deepwelldevelopment.spacequest.engine.EngineContext;
import com.deepwelldevelopment.spacequest.engine.graph.Renderer;
import com.deepwelldevelopment.spacequest.engine.scene.Camera;
import com.deepwelldevelopment.spacequest.engine.scene.Scene;
import com.deepwelldevelopment.spacequest.engine.window.KeyboardInput;
import com.deepwelldevelopment.spacequest.engine.window.MouseInput;
import com.deepwelldevelopment.spacequest.engine.window.Window;
import com.deepwelldevelopment.world.World;

public class SpaceQuest {

    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 0.01f;

    private World world;

    public World init(EngineContext engineContext, Renderer renderer) {
        world = new World();
        world.generate();

        Camera camera = engineContext.scene().getCamera();
        camera.setPosition(0.0f, 20.0f, 0.0f);
        camera.setRotation((float) Math.toRadians(20.0f), (float) Math.toRadians(90.f));

        return world;
    }

    public void input(EngineContext engineContext, long deltaTime) {
        Scene scene = engineContext.scene();
        Window window = engineContext.window();

        KeyboardInput ki = window.getKeyboardInput();
        float move = deltaTime * MOVEMENT_SPEED;
        Camera camera = scene.getCamera();
        if (ki.keyPressed(GLFW_KEY_W)) {
            camera.moveForward(move);
        } else if (ki.keyPressed(GLFW_KEY_S)) {
            camera.moveBackwards(move);
        }
        if (ki.keyPressed(GLFW_KEY_A)) {
            camera.moveLeft(move);
        } else if (ki.keyPressed(GLFW_KEY_D)) {
            camera.moveRight(move);
        }
        if (ki.keyPressed(GLFW_KEY_SPACE)) {
            camera.moveUp(move);
        } else if (ki.keyPressed(GLFW_KEY_LEFT_SHIFT)) {
            camera.moveDown(move);
        }

        if (ki.keySinglePress(GLFW_KEY_ESCAPE)) {
            window.getMouseInput().toggleMouseLock();
        }

        MouseInput mi = window.getMouseInput();
        if (mi.isMouseLocked()) {
            Vector2f deltaPos = mi.getDeltaPos();
            camera.addRotation((float) Math.toRadians(deltaPos.y * MOUSE_SENSITIVITY),
                    (float) Math.toRadians(deltaPos.x * MOUSE_SENSITIVITY));
        }

        ki.reset();
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
