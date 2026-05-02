package com.petmgt.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FileUtil {

    private static final int THUMB_WIDTH = 300;
    private static final int THUMB_HEIGHT = 300;

    public static String createThumbnail(String originalPath) throws IOException {
        File originalFile = new File(originalPath);
        BufferedImage originalImage = ImageIO.read(originalFile);
        if (originalImage == null) {
            throw new IOException("无法读取图片文件");
        }

        BufferedImage thumbnail = new BufferedImage(THUMB_WIDTH, THUMB_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnail.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, THUMB_WIDTH, THUMB_HEIGHT, null);
        g2d.dispose();

        String thumbPath = originalFile.getParent() + File.separator + "thumb_" + originalFile.getName();
        ImageIO.write(thumbnail, "jpg", new File(thumbPath));
        return "thumb_" + originalFile.getName();
    }
}
