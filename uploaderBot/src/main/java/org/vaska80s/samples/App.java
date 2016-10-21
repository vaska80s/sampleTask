package org.vaska80s.samples;


import com.drew.imaging.ImageProcessingException;
import org.apache.log4j.Logger;
import org.vaska80s.samples.resizer.Resizer;

import java.io.File;
import java.io.IOException;

/**
 * Main class
 */
public class App {
    private final static Logger log = Logger.getLogger(App.class);

    private final static int OUT_WIDTH = 640;
    private final static int OUT_HEIGHT = 640;

    public static void main(String[] args) {
        try {
            Resizer resizer = new Resizer(OUT_WIDTH, OUT_HEIGHT);
            resizer.resize(new File("../27388.jpg"), new File("../resized.jpg"));
            resizer.resize(new File("../RM.png"), new File("../RM-resized.png"));
            resizer.resize(new File("../fit.jpg"), new File("../fit-resized.jpg"));
            resizer.resize(new File("../20160622_222108.jpg"), new File("../222108-res.jpg"));
            resizer.resize(new File("../20160622_222108.png"), new File("../222108-res.png"));
            Resizer hiResResizer = new Resizer(5600, 600);
            hiResResizer.resize(new File("../27388.jpg"), new File("../hiRes.jpg"));
        } catch (IOException | ImageProcessingException e) {
            log.error(e);
        }
    }
}
