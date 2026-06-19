package com.reader.util;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

@Component
public class ImageUtil {

    private static final Map<String, String> CONTENT_TYPES = Map.of(
            ".jpg", "image/jpeg",
            ".jpeg", "image/jpeg",
            ".png", "image/png",
            ".webp", "image/webp",
            ".gif", "image/gif",
            ".bmp", "image/bmp"
    );

    private static final Set<String> IMAGE_EXTENSIONS = CONTENT_TYPES.keySet();

    /**
     * Generates a JPEG thumbnail from an image stream, resized proportionally to the target width.
     */
    public byte[] generateThumbnail(InputStream imageStream, int targetWidth) throws IOException {
        BufferedImage originalImage = ImageIO.read(imageStream);
        if (originalImage == null) {
            throw new IOException("Unable to read image");
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        if (originalWidth <= 0 || originalHeight <= 0) {
            throw new IOException("Invalid image dimensions");
        }

        int targetHeight = (int) ((double) originalHeight / originalWidth * targetWidth);
        if (targetHeight <= 0) {
            targetHeight = 1;
        }

        BufferedImage thumbnail = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnail.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(thumbnail, "jpg", outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Returns the image dimensions as [width, height].
     */
    public int[] getImageDimensions(InputStream imageStream) throws IOException {
        BufferedImage image = ImageIO.read(imageStream);
        if (image == null) {
            throw new IOException("Unable to read image");
        }
        return new int[]{image.getWidth(), image.getHeight()};
    }

    /**
     * Checks if the given filename has an image extension.
     */
    public boolean isImageFile(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        for (String ext : IMAGE_EXTENSIONS) {
            if (lower.endsWith(ext)) return true;
        }
        return false;
    }

    /**
     * Returns the MIME content type for a given filename based on its extension.
     */
    public String getContentType(String fileName) {
        if (fileName == null) return "application/octet-stream";
        String lower = fileName.toLowerCase();
        for (Map.Entry<String, String> entry : CONTENT_TYPES.entrySet()) {
            if (lower.endsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "application/octet-stream";
    }
}
