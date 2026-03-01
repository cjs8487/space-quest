package com.deepwelldevelopment.spacequest.engine.window;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CLIENT_API;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_MAXIMIZED;
import static org.lwjgl.glfw.GLFW.GLFW_NO_API;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;

public class Window {

    private long handle;
    private final KeyboardInput keyboardInput;
    private final MouseInput mouseInput;
    private int width;
    private int height;

    public Window(String title) {
        if (!glfwInit()) {
            throw new RuntimeException("Unable to initialize GLFW");
        }

        if (!glfwVulkanSupported()) {
            throw new RuntimeException("Vulkan is not supported");
        }

        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vidMode == null) {
            throw new RuntimeException("Failed to get video mode");
        }

        width = vidMode.width();
        height = vidMode.height();

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_FALSE);

        handle = glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (handle == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        keyboardInput = new KeyboardInput(handle);
        glfwSetFramebufferSizeCallback(handle, (window, width, height) -> {
            // Handle framebuffer resize
            this.width = width;
            this.height = height;
        });
        mouseInput = new MouseInput(handle);
    }

    public void cleanup() {
        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);
        glfwTerminate();
    }

    public long getHandle() {
        return handle;
    }

    public KeyboardInput getKeyboardInput() {
        return keyboardInput;
    }

    public MouseInput getMouseInput() {
        return mouseInput;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void pollEvents() {
        keyboardInput.input();
        mouseInput.input();
    }

    public void resetInput() {
        keyboardInput.reset();
    }

    public void setShouldClose(boolean shouldClose) {
        glfwSetWindowShouldClose(handle, shouldClose);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }
}
