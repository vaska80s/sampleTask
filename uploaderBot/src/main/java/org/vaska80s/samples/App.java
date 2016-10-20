package org.vaska80s.samples;


import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Hello world!
 */
public class App {
    private final static Logger log = Logger.getLogger(App.class);

    public static void main(String[] args) {
        File imageFile = new File("../../../Pictures/27388.jpg");

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
            int xSubsampling = (int)Math.ceil(reader.getWidth(imageIndex)/640F);
            int ySubsampling = (int)Math.ceil(reader.getHeight(imageIndex)/640F);
            param.setSourceSubsampling(xSubsampling, ySubsampling, 0, 0);

            BufferedImage bi = reader.read(imageIndex, param);

            File outputfile = new File("../../../Pictures/27388-proc.jpg");
            ImageIO.write(bi, reader.getFormatName(), outputfile);
        } catch (IOException e) {
            log.error(e);
        }
    }
}
