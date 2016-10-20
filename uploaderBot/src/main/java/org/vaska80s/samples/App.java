package org.vaska80s.samples;


import org.apache.log4j.Logger;

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
 * Hello world!
 */
public class App {
    private final static Logger log = Logger.getLogger(App.class);

    private final static int OUT_WIDTH = 640;
    private final static int OUT_HEIGHT = 640;

    public static void main(String[] args) {
        File imageFile = new File("../27388.jpg");

        try {
            ImageInputStream iis = ImageIO.createImageInputStream(imageFile);
            Iterator iterator = ImageIO.getImageReaders(iis);
            if(!iterator.hasNext()){
                System.out.println("Unsupported file format " + imageFile.getName());
            }
            ImageReader reader = (ImageReader) iterator.next();
            reader.setInput(iis);
            ImageReadParam param = reader.getDefaultReadParam();
            int imageIndex = 0;
            int xSubsampling = reader.getWidth(imageIndex)/OUT_WIDTH;
            int ySubsampling = reader.getHeight(imageIndex)/OUT_HEIGHT;
            param.setSourceSubsampling(xSubsampling, ySubsampling, 0, 0);

            BufferedImage bi = reader.read(imageIndex, param);

            File outputfile = new File("../27388-proc.jpg");
            ImageIO.write(bi, reader.getFormatName(), outputfile);

            BufferedImage outImage = getBackgroundImage(bi.getType());
            outImage.getGraphics().drawImage(bi, 0, 0, OUT_WIDTH, OUT_HEIGHT, 0, 0, bi.getWidth(), bi.getHeight(), null);
            File bgFile = new File("../bg.jpg");
            ImageIO.write(outImage, reader.getFormatName(), bgFile);
        } catch (IOException e) {
            log.error(e);
        }
    }

    private static BufferedImage getBackgroundImage(int imageType){
        BufferedImage img = new BufferedImage(OUT_WIDTH, OUT_HEIGHT, imageType);
        Graphics g = img.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, OUT_WIDTH, OUT_HEIGHT);

        return img;
    }
}
