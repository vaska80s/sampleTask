package org.vaska80s.samples.agents;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
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
     *
     * @param targetXSize target image width
     * @param targetYSize target image height
     */
    public Resizer(int targetXSize, int targetYSize) {
        this.targetXSize = targetXSize;
        this.targetYSize = targetYSize;
    }

    /**
     * Perform resize.
     *
     * @param original original <code>File</code> of source image.
     * @param resized  a <code>File</code> the result to be written to.
     * @throws IOException if an error occurs reading or writing image of image format is not supported.
     */
    public void resize(File original, File resized)
            throws IOException, ImageProcessingException {
        BufferedImage image = readImage(original);
        ImageIO.write(resize(image), formatName, resized);
    }

    private BufferedImage readImage(File imageFile)
            throws IOException, ImageProcessingException {
        if (!imageFile.exists()) {
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

        if (subsampling > 1) {
            param.setSourceSubsampling(subsampling, subsampling, 0, 0);
        }

        formatName = reader.getFormatName();

        BufferedImage bufferedImage = reader.read(imageIndex, param);
        iis.close();
        return rotate(bufferedImage, imageFile);
    }

    private BufferedImage resize(BufferedImage originalImage) {
        int xOffset = 0;
        int yOffset = 0;

        int oWidth = originalImage.getWidth();
        int oHeight = originalImage.getHeight();

        float oAspectRatio = (float) originalImage.getWidth() / originalImage.getHeight();
        float tAspectRatio = (float) targetXSize / targetYSize;

        if (oAspectRatio > tAspectRatio) {
            Float scale = targetXSize / (float) oWidth;
            yOffset = (int) (targetYSize - oHeight * scale) / 2;
        } else if (tAspectRatio > oAspectRatio) {
            Float scale = targetYSize / (float) oHeight;
            xOffset = (int) (targetXSize - oWidth * scale) / 2;
        }

        BufferedImage outImage = new BufferedImage(targetXSize, targetYSize, originalImage.getType());
        Graphics g = outImage.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, targetXSize, targetYSize);

        outImage.getGraphics().drawImage(originalImage,
                xOffset, yOffset, targetXSize - xOffset, targetYSize - yOffset,
                0, 0, oWidth, oHeight,
                null);

        return outImage;
    }

    private BufferedImage rotate(BufferedImage inImage, File imageFile)
            throws ImageProcessingException, IOException {
        Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
        Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

        if(directory == null){
            return inImage;
        }

        int orientation;
        try {
            orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
        } catch (MetadataException me) {
            return inImage;
        }

        AffineTransform t = new AffineTransform();

        switch (orientation) {
            case 1:
                break;
            case 2: //Flip X
                t.scale(-1.0, 1.0);
                t.translate(-inImage.getWidth(), 0);
                break;
            case 3: //PI rotation
                t.translate(inImage.getWidth(), inImage.getHeight());
                t.rotate(Math.PI);
                break;
            case 4: //Flip Y
                t.scale(1.0, -1.0);
                t.translate(0, -inImage.getHeight());
                break;
            case 5: //-PI/2 and Flip X
                t.rotate(-Math.PI / 2);
                t.scale(-1.0, 1.0);
                break;
            case 6: //-PI/2 and -width
                t.translate(inImage.getHeight(), 0);
                t.rotate(Math.PI / 2);
                break;
            case 7: //PI/2 and Flip
                t.scale(-1.0, 1.0);
                t.translate(-inImage.getHeight(), 0);
                t.translate(0, inImage.getWidth());
                t.rotate(3 * Math.PI / 2);
                break;
            case 8: //PI/2
                t.translate(0, inImage.getWidth());
                t.rotate(3 * Math.PI / 2);
                break;
        }

        AffineTransformOp op = new AffineTransformOp(t, AffineTransformOp.TYPE_BICUBIC);

        BufferedImage outImage = new BufferedImage(inImage.getHeight(), inImage.getWidth(), inImage.getType());
        Graphics2D g = outImage.createGraphics();
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, outImage.getWidth(), outImage.getHeight());
        outImage = op.filter(inImage, outImage);

        return outImage;
    }
}
