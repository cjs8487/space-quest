package com.deepwelldevelopment.spacequest.engine.window;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_2;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwSetCursorEnterCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;

import org.joml.Vector2f;

public class MouseInput {
    private final Vector2f currentPos;
    private final Vector2f deltaPos;
    private final Vector2f previousPos;
    private boolean isInWindow;
    private boolean leftButtonPressed;
    private boolean rightButtonPressed;
    private boolean leftButtonSinglePress;
    private boolean rightButtonSinglePress;
    private boolean mouseLocked;
    private final long windowHandle;

    public MouseInput(long windowHandle) {
        this.windowHandle = windowHandle;
        this.currentPos = new Vector2f(-1, -1);
        this.deltaPos = new Vector2f();
        this.previousPos = new Vector2f();
        this.isInWindow = true;
        this.leftButtonPressed = false;
        this.rightButtonPressed = false;
        this.leftButtonSinglePress = false;
        this.rightButtonSinglePress = false;
        this.mouseLocked = false;

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

            // Set single press flags on press events
            System.out.println("Mouse button: " + button + ", action: " + action);
            if (action == GLFW_PRESS) {
                if (button == GLFW_MOUSE_BUTTON_1) {
                    leftButtonSinglePress = true;
                } else if (button == GLFW_MOUSE_BUTTON_2) {
                    rightButtonSinglePress = true;
                }
            } else if (action == GLFW_RELEASE) {
                if (button == GLFW_MOUSE_BUTTON_1) {
                    leftButtonSinglePress = false;
                } else if (button == GLFW_MOUSE_BUTTON_2) {
                    rightButtonSinglePress = false;
                }
            }
        });

        this.toggleMouseLock();
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

        if (mouseLocked) {
            centerCursor();
        }
    }

    public boolean isLeftButtonPressed() {
        return leftButtonPressed;
    }

    public boolean isRightButtonPressed() {
        return rightButtonPressed;
    }

    public boolean isLeftButtonSinglePress() {
        return leftButtonSinglePress;
    }

    public boolean isRightButtonSinglePress() {
        return rightButtonSinglePress;
    }

    public boolean isMouseLocked() {
        return mouseLocked;
    }

    public void toggleMouseLock() {
        mouseLocked = !mouseLocked;
        if (mouseLocked) {
            glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            centerCursor();
        } else {
            glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }
    }

    public void setMouseLocked(boolean locked) {
        if (mouseLocked != locked) {
            toggleMouseLock();
        }
    }

    private void centerCursor() {
        int[] width = new int[1];
        int[] height = new int[1];
        glfwGetWindowSize(windowHandle, width, height);
        float centerX = width[0] / 2.0f;
        float centerY = height[0] / 2.0f;
        glfwSetCursorPos(windowHandle, centerX, centerY);
        currentPos.set(centerX, centerY);
        previousPos.set(centerX, centerY);
    }
}
