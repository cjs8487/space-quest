package com.deepwelldevelopment.spacequest.engine.graph.vk;

import com.deepwelldevelopment.spacequest.engine.EngineConfig;
import com.deepwelldevelopment.spacequest.engine.window.Window;

public class VulkanContext {

    private final Device device;
    private final Instance instance;
    private final PhysicalDevice physicalDevice;
    private Surface surface;

    public VulkanContext(Window window) {
        var engCfg = EngineConfig.getInstance();
        instance = new Instance(engCfg.isVkValidate());
        physicalDevice = PhysicalDevice.createPhysicalDevice(instance, engCfg.getPhysicalDeviceName());
        device = new Device(physicalDevice);
        surface = new Surface(instance, physicalDevice, window);
    }

    public void cleanup() {
        surface.cleanup(instance);
        device.cleanup();
        physicalDevice.cleanup();
        instance.cleanup();
    }

    public Device getDevice() {
        return device;
    }

    public PhysicalDevice getPhysicalDevice() {
        return physicalDevice;
    }

    public Surface getSurface() {
        return surface;
    }
}
