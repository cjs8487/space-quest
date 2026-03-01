package com.deepwelldevelopment.spacequest.engine.window;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;

import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;

public class KeyboardInput implements GLFWKeyCallbackI {
    private final Map<Integer, Boolean> singleKeyPressMap;
    private final long windowHandle;
    private List<GLFWKeyCallbackI> callbacks;

    public KeyboardInput(long windowHandle) {
        this.windowHandle = windowHandle;
        this.singleKeyPressMap = new java.util.HashMap<>();
        glfwSetKeyCallback(windowHandle, this);
        this.callbacks = new java.util.ArrayList<>();
    }

    public void addKeyCallback(GLFWKeyCallbackI callback) {
        this.callbacks.add(callback);
    }

    public void input() {
        glfwPollEvents();
    }

    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
        singleKeyPressMap.put(key, action == GLFW_PRESS);
        int numCallbacks = callbacks.size();
        for (int i = 0; i < numCallbacks; i++) {
            callbacks.get(i).invoke(window, key, scancode, action, mods);
        }
    }

    public boolean keyPressed(int key) {
        return glfwGetKey(windowHandle, key) == GLFW_PRESS;
    }

    public boolean keySinglePress(int key) {
        return singleKeyPressMap.getOrDefault(key, false);
    }

    public void reset() {
        singleKeyPressMap.clear();
    }

    public void setCharCallback(GLFWCharCallbackI callback) {
        glfwSetCharCallback(windowHandle, callback);
    }
}
