package com.deepwelldevelopment.spacequest.engine.graph.vk;

import static org.lwjgl.util.vma.Vma.VMA_ALLOCATION_CREATE_DEDICATED_MEMORY_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_DEPTH_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;

public class Attachment {

    private final Image image;
    private final ImageView imageView;
    private boolean depthAttachment;

    public Attachment(VulkanContext context, int width, int height, int format, int usage) {
        var imageData = new Image.ImageData().width(width).height(height).usage(usage | VK_IMAGE_USAGE_SAMPLED_BIT)
                .format(format).memUsage(VMA_ALLOCATION_CREATE_DEDICATED_MEMORY_BIT);
        image = new Image(context, imageData);

        int aspectMask = 0;
        if ((usage & VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT) > 0) {
            aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
            depthAttachment = false;
        }
        if ((usage & VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT) > 0) {
            aspectMask = VK_IMAGE_ASPECT_DEPTH_BIT;
            depthAttachment = true;
        }

        var imageViewData = new ImageView.ImageViewData().format(image.getFormat()).aspectMask(aspectMask);
        imageView = new ImageView(context.getDevice(), image.getVkImage(), imageViewData, depthAttachment);
    }

    public void cleanup(VulkanContext VulkanContext) {
        imageView.cleanup(VulkanContext.getDevice());
        image.cleanup(VulkanContext);
    }

    public Image getImage() {
        return image;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public boolean isDepthAttachment() {
        return depthAttachment;
    }
}
