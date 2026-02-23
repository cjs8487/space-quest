package com.deepwelldevelopment.spacequest.engine.window;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_2;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwSetCursorEnterCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;

import org.joml.Vector2f;

public class MouseInput {
    private final Vector2f currentPos;
    private final Vector2f deltaPos;
    private final Vector2f previousPos;
    private boolean isInWindow;
    private boolean leftButtonPressed;
    private boolean rightButtonPressed;

    public MouseInput(long windowHandle) {
        this.currentPos = new Vector2f(-1, -1);
        this.deltaPos = new Vector2f();
        this.previousPos = new Vector2f();
        this.isInWindow = false;
        this.leftButtonPressed = false;
        this.rightButtonPressed = false;

        glfwSetCursorPosCallback(windowHandle, (handle, x, y) -> {
            currentPos.x = (float) x;
            currentPos.y = (float) y;
        });
        glfwSetCursorEnterCallback(windowHandle, (handle, entered) -> {
            isInWindow = entered;
        });
        glfwSetMouseButtonCallback(windowHandle, (handle, button, action, mods) -> {
            leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
        });
    }

    public Vector2f getCurrentPos() {
        return currentPos;
    }

    public Vector2f getDeltaPos() {
        return deltaPos;
    }

    public void input() {
        deltaPos.x = 0;
        deltaPos.y = 0;
        if (previousPos.x >= 0 && previousPos.y >= 0 && isInWindow) {
            deltaPos.x = currentPos.x - previousPos.x;
            deltaPos.y = currentPos.y - previousPos.y;
        }
        previousPos.x = currentPos.x;
        previousPos.y = currentPos.y;
    }

    public boolean isLeftButtonPressed() {
        return leftButtonPressed;
    }

    public boolean isRightButtonPressed() {
        return rightButtonPressed;
    }
}
