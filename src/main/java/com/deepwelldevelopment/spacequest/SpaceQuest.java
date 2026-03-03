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
import com.deepwelldevelopment.spacequest.engine.graphics.Renderer;
import com.deepwelldevelopment.spacequest.engine.physics.Ray;
import com.deepwelldevelopment.spacequest.engine.physics.RaycastResult;
import com.deepwelldevelopment.spacequest.engine.physics.RaycasterUtil;
import com.deepwelldevelopment.spacequest.engine.scene.Camera;
import com.deepwelldevelopment.spacequest.engine.scene.Scene;
import com.deepwelldevelopment.spacequest.engine.window.KeyboardInput;
import com.deepwelldevelopment.spacequest.engine.window.MouseInput;
import com.deepwelldevelopment.spacequest.engine.window.Window;
import com.deepwelldevelopment.spacequest.world.World;

import com.deepwelldevelopment.spacequest.block.Blocks;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;

public class SpaceQuest {

    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 0.01f;

    private World world;

    public World init(EngineContext engineContext, Renderer renderer) {
        world = new World();
        world.setRenderer(renderer);
        world.setScene(engineContext.scene());
        world.generate();

        Camera camera = engineContext.scene().getCamera();
        camera.setPosition(0.0f, 20.0f, 0.0f);
        camera.setRotation((float) Math.toRadians(20.0f), (float) Math.toRadians(90.f));

        return world;
    }

    public void input(EngineContext engineContext, long deltaTime) {
        if (handleGui(engineContext)) {
            return;
        }

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

            // Handle block interaction
            handleBlockInteraction(mi, camera);
        }

        ki.reset();
    }

    public void update(EngineContext engineContext, long deltaTime) {
        this.world.tick(engineContext.scene().getCamera().getPosition());
    }

    public void cleanup() {
    }

    private void handleBlockInteraction(MouseInput mouseInput, Camera camera) {
        if (mouseInput.isLeftButtonSinglePress()) {
            Ray ray = RaycasterUtil.getForwardRay(camera);
            RaycastResult result = world.raycast(ray, 5.0f);

            if (result.hit) {
                int placeX = (int) result.blockPosition.x + (int) result.hitNormal.x;
                int placeY = (int) result.blockPosition.y + (int) result.hitNormal.y;
                int placeZ = (int) result.blockPosition.z + (int) result.hitNormal.z;
                System.out.println(result.hitNormal);

                if (world.getBlock(placeX, placeY, placeZ) == Blocks.AIR) {
                    world.setBlock(placeX, placeY, placeZ, Blocks.STONE);
                }
            }
        }

        if (mouseInput.isRightButtonSinglePress()) {
            Ray ray = RaycasterUtil.getForwardRay(camera);
            RaycastResult result = world.raycast(ray, 5.0f);

            if (result.hit) {
                int removeX = (int) result.blockPosition.x;
                int removeY = (int) result.blockPosition.y;
                int removeZ = (int) result.blockPosition.z;

                world.setBlock(removeX, removeY, removeZ, Blocks.AIR);
            }
        }
    }

    private boolean handleGui(EngineContext engCtx) {
        ImGuiIO imGuiIO = ImGui.getIO();
        MouseInput mouseInput = engCtx.window().getMouseInput();
        Vector2f mousePos = mouseInput.getCurrentPos();
        imGuiIO.addMousePosEvent(mousePos.x, mousePos.y);
        imGuiIO.addMouseButtonEvent(0, mouseInput.isLeftButtonPressed());
        imGuiIO.addMouseButtonEvent(1, mouseInput.isRightButtonPressed());

        ImGui.newFrame();
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(200, 250);
        ImGui.begin("Overlay", ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoInputs);

        ImGui.text(String.format("X: %.2f, Y: %.2f, Z: %.2f", engCtx.scene().getCamera().getPosition().x,
                engCtx.scene().getCamera().getPosition().y, engCtx.scene().getCamera().getPosition().z));
        ImGui.newLine();
        ImGui.text("Chunk: " + Math.floorDiv((int) engCtx.scene().getCamera().getPosition().x, 16) + ", "
                + Math.floorDiv((int) engCtx.scene().getCamera().getPosition().z, 16));
        ImGui.newLine();
        ImGui.text("FPS: " + engCtx.engine().getFps());
        ImGui.text("UPS: " + engCtx.engine().getUps());

        ImGui.end();
        ImGui.render();
        ImGui.endFrame();

        return false; // Never block input
    }

    public static void main(String[] args) {
        Logger.info("Starting Space Quest...");
        var engine = new Engine("Space Quest", new SpaceQuest());
        Logger.info("Started Space Quest");
        engine.run();
    }
}
