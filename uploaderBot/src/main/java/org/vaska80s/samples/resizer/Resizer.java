package org.vaska80s.samples.resizer;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Vasiliy Serov 21.10.2016.
 */
public class Resizer {
    private int targetXSize;
    private int targetYSize;

    private String formatName;

    /**
     * Constructor.
     * @param targetXSize target image width
     * @param targetYSize target image height
     */
    public Resizer(int targetXSize, int targetYSize) {
        this.targetXSize = targetXSize;
        this.targetYSize = targetYSize;
    }

    /**
     * Perform resize.
     * @param original original <code>File</code> of source image.
     * @param resized a <code>File</code> the result to be written to.
     * @throws IOException if an error occurs reading or writing image of image format is not supported.
     */
    public void resize(File original, File resized) throws IOException {
        BufferedImage image = readImage(original);
        ImageIO.write(resize(image), formatName, resized);
    }

    private BufferedImage readImage(File imageFile) throws IOException {
        if(!imageFile.exists()){
            throw new IOException("File " + imageFile.getName() + " does not exist");
        }
        ImageInputStream iis = ImageIO.createImageInputStream(imageFile);
        Iterator iterator = ImageIO.getImageReaders(iis);

        if (!iterator.hasNext()) {
            throw new IOException("Unsupported image format " + imageFile.getName());
        }

        ImageReader reader = (ImageReader) iterator.next();
        reader.setInput(iis);
        ImageReadParam param = reader.getDefaultReadParam();
        int imageIndex = 0;
        int subsampling;
        if (reader.getWidth(imageIndex) > reader.getHeight(imageIndex)) {
            subsampling = reader.getWidth(imageIndex) / targetXSize;
        } else {
            subsampling = reader.getHeight(imageIndex) / targetYSize;
        }

        if(subsampling > 1) {
            param.setSourceSubsampling(subsampling, subsampling, 0, 0);
        }

        formatName = reader.getFormatName();

        return reader.read(imageIndex, param);
    }

    private BufferedImage resize(BufferedImage originalImage){
        int xOffset = 0;
        int yOffset = 0;

        int oWidth = originalImage.getWidth();
        int oHeight = originalImage.getHeight();

        float oAspectRatio = (float)originalImage.getWidth()/originalImage.getHeight();
        float tAspectRatio = (float)targetXSize/targetYSize;

        if (oAspectRatio > tAspectRatio) {
            Float scale = targetXSize / (float) oWidth;
            yOffset = (int)(targetYSize - oHeight * scale)/2;
        } else if (tAspectRatio > oAspectRatio) {
            Float scale = targetYSize / (float) oHeight;
            xOffset = (int)(targetXSize - oWidth * scale)/2;
        }

        BufferedImage outImage = new BufferedImage(targetXSize, targetYSize, originalImage.getType());
        Graphics g = outImage.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, targetXSize, targetYSize);

        outImage.getGraphics().drawImage(originalImage,
                xOffset, yOffset, targetXSize-xOffset, targetYSize-yOffset,
                0, 0, oWidth, oHeight,
                null);

        return outImage;
    }
}
