package com.deepwelldevelopment.spacequest.engine.graphics;

import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;

import com.deepwelldevelopment.spacequest.engine.graphics.vk.ImageSrc;

public class GraphicsUtils {

    private GraphicsUtils() {
        // Utility class
    }

    public static void cleanImageData(ImageSrc srcImage) {
        stbi_image_free(srcImage.data());
    }

    public static ImageSrc loadImage(String fileName) throws IOException {
        ImageSrc srcImage;
        ByteBuffer buf;
        try (var stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            buf = stbi_load(fileName, w, h, channels, 4);
            if (buf == null) {
                throw new IOException("Image file [" + fileName + "] not loaded: " + stbi_failure_reason());
            }

            srcImage = new ImageSrc(buf, w.get(0), h.get(0), channels.get(0));
        }

        return srcImage;
    }
}
