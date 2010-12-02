package helpers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import play.libs.Images;

public class ImageHelper {

    public static void resizeRespectingRatio(File from, File to, int width, int height) {
        try {
            int squareSize = 0;
            BufferedImage source = ImageIO.read(from);
            if (source.getHeight() > source.getWidth()) {
                squareSize = source.getWidth();
            } else {
                squareSize = source.getHeight();
            }
            int startX = (source.getWidth() - squareSize) / 2;
            int startY = (source.getHeight() - squareSize) / 2;
            int endX = startX + squareSize;
            int endY = startY + squareSize;
            Images.crop(from, to, startX, startY, endX, endY);
            Images.resize(to, to, width, height);
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
        
    }
    
}
