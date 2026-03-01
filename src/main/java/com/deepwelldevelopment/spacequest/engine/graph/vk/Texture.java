package com.deepwelldevelopment.spacequest.engine.graph.vk;

import static com.deepwelldevelopment.spacequest.MathUtils.log2;
import static org.lwjgl.util.vma.Vma.VMA_ALLOCATION_CREATE_HOST_ACCESS_SEQUENTIAL_WRITE_BIT;
import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_AUTO;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_TRANSFER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_TRANSFER_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_FAMILY_IGNORED;
import static org.lwjgl.vulkan.VK10.vkCmdBlitImage;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBufferToImage;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_NONE;
import static org.lwjgl.vulkan.VK13.vkCmdPipelineBarrier2;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkDependencyInfo;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageMemoryBarrier2;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkOffset3D;

public class Texture {

    private final int height;
    private final String id;
    private final Image image;
    private final ImageView imageView;
    private final int width;
    private boolean recordedTransition;
    private VulkanBuffer stgBuffer;
    private boolean transparent;

    public Texture(VulkanContext vulkanContext, String id, ImageSrc srcImage, int imageFormat) {
        this.id = id;
        recordedTransition = false;
        width = srcImage.width();
        height = srcImage.height();

        setTransparent(srcImage.data());
        createStgBuffer(vulkanContext, srcImage.data());
        int mipLevels = (int) Math.floor(log2(Math.min(width, height))) + 1;
        var imageData = new Image.ImageData().width(width).height(height)
                .usage(VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT)
                .format(imageFormat).mipLevels(mipLevels);
        image = new Image(vulkanContext, imageData);
        var imageViewData = new ImageView.ImageViewData().format(image.getFormat())
                .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevels(mipLevels);
        imageView = new ImageView(vulkanContext.getDevice(), image.getVkImage(), imageViewData, false);
    }

    public void cleanup(VulkanContext vulkanContext) {
        cleanupStgBuffer(vulkanContext);
        imageView.cleanup(vulkanContext.getDevice());
        image.cleanup(vulkanContext);
    }

    public void cleanupStgBuffer(VulkanContext vulkanContext) {
        if (stgBuffer != null) {
            stgBuffer.cleanup(vulkanContext);
            stgBuffer = null;
        }
    }

    private void createStgBuffer(VulkanContext vulkanContext, ByteBuffer data) {
        int size = data.remaining();
        stgBuffer = new VulkanBuffer(vulkanContext, size, VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VMA_MEMORY_USAGE_AUTO,
                VMA_ALLOCATION_CREATE_HOST_ACCESS_SEQUENTIAL_WRITE_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
        long mappedMemory = stgBuffer.map(vulkanContext);
        ByteBuffer buffer = MemoryUtil.memByteBuffer(mappedMemory, (int) stgBuffer.getRequestedSize());
        buffer.put(data);
        data.flip();

        stgBuffer.unmap(vulkanContext);
    }

    public int getHeight() {
        return height;
    }

    public String getId() {
        return id;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public int getWidth() {
        return width;
    }

    public boolean isTransparent() {
        return transparent;
    }

    private void recordCopyBuffer(MemoryStack stack, CommandBuffer cmd, VulkanBuffer bufferData) {
        VkBufferImageCopy.Buffer region = VkBufferImageCopy.calloc(1, stack).bufferOffset(0).bufferRowLength(0)
                .bufferImageHeight(0)
                .imageSubresource(
                        it -> it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(0).baseArrayLayer(0).layerCount(1))
                .imageOffset(it -> it.x(0).y(0).z(0)).imageExtent(it -> it.width(width).height(height).depth(1));

        vkCmdCopyBufferToImage(cmd.getVkCommandBuffer(), bufferData.getBuffer(), image.getVkImage(),
                VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, region);
    }

    private void setTransparent(ByteBuffer data) {
        int numPixels = data.capacity() / 4;
        int offset = 0;
        transparent = false;
        for (int i = 0; i < numPixels; i++) {
            int a = (0xFF & data.get(offset + 3));
            if (a < 255) {
                transparent = true;
                break;
            }
            offset += 4;
        }
    }

    private void recordGenerateMipMaps(MemoryStack stack, CommandBuffer cmd) {
        VkImageSubresourceRange subResourceRange = VkImageSubresourceRange.calloc(stack)
                .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).baseArrayLayer(0).levelCount(1).layerCount(1);

        VkImageMemoryBarrier2.Buffer barrier = VkImageMemoryBarrier2.calloc(1, stack).sType$Default()
                .image(image.getVkImage()).srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED).subresourceRange(subResourceRange);

        VkDependencyInfo depInfo = VkDependencyInfo.calloc(stack).sType$Default().pImageMemoryBarriers(barrier);

        int mipWidth = width;
        int mipHeight = height;

        int mipLevels = image.getMipLevels();
        for (int i = 1; i < mipLevels; i++) {
            subResourceRange.baseMipLevel(i - 1);
            barrier.subresourceRange(subResourceRange).oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
                    .newLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL).srcStageMask(VK_PIPELINE_STAGE_TRANSFER_BIT)
                    .dstStageMask(VK_PIPELINE_STAGE_TRANSFER_BIT).srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
                    .dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT);

            vkCmdPipelineBarrier2(cmd.getVkCommandBuffer(), depInfo);

            int auxi = i;
            VkOffset3D srcOffset0 = VkOffset3D.calloc(stack).x(0).y(0).z(0);
            VkOffset3D srcOffset1 = VkOffset3D.calloc(stack).x(mipWidth).y(mipHeight).z(1);
            VkOffset3D dstOffset0 = VkOffset3D.calloc(stack).x(0).y(0).z(0);
            VkOffset3D dstOffset1 = VkOffset3D.calloc(stack).x(mipWidth > 1 ? mipWidth / 2 : 1)
                    .y(mipHeight > 1 ? mipHeight / 2 : 1).z(1);
            VkImageBlit.Buffer blit = VkImageBlit.calloc(1, stack).srcOffsets(0, srcOffset0).srcOffsets(1, srcOffset1)
                    .srcSubresource(it -> it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(auxi - 1).baseArrayLayer(0)
                            .layerCount(1))
                    .dstOffsets(0, dstOffset0).dstOffsets(1, dstOffset1).dstSubresource(it -> it
                            .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(auxi).baseArrayLayer(0).layerCount(1));

            vkCmdBlitImage(cmd.getVkCommandBuffer(), image.getVkImage(), VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    image.getVkImage(), VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, blit, VK_FILTER_LINEAR);

            barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL).newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                    .srcAccessMask(VK_ACCESS_TRANSFER_READ_BIT).dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

            barrier.srcStageMask(VK_PIPELINE_STAGE_TRANSFER_BIT).dstStageMask(VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT);

            vkCmdPipelineBarrier2(cmd.getVkCommandBuffer(), depInfo);

            if (mipWidth > 1)
                mipWidth /= 2;
            if (mipHeight > 1)
                mipHeight /= 2;
        }

        barrier.subresourceRange(it -> it.baseMipLevel(mipLevels - 1)).oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
                .newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL).srcStageMask(VK_PIPELINE_STAGE_TRANSFER_BIT)
                .dstStageMask(VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT).srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
                .dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

        vkCmdPipelineBarrier2(cmd.getVkCommandBuffer(), depInfo);
    }

    public void recordTextureTransition(CommandBuffer cmd) {
        if (stgBuffer != null && !recordedTransition) {
            recordedTransition = true;
            try (var stack = MemoryStack.stackPush()) {
                VulkanUtils.imageBarrier(stack, cmd.getVkCommandBuffer(), image.getVkImage(), VK_IMAGE_LAYOUT_UNDEFINED,
                        VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
                        VK_PIPELINE_STAGE_TRANSFER_BIT, VK_ACCESS_2_NONE, VK_ACCESS_TRANSFER_WRITE_BIT,
                        VK_IMAGE_ASPECT_COLOR_BIT);
                recordCopyBuffer(stack, cmd, stgBuffer);
                recordGenerateMipMaps(stack, cmd);
            }
        }
    }
}
