package org.vaska80s.samples;


import com.drew.imaging.ImageProcessingException;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.users.FullAccount;
import org.apache.log4j.Logger;
import org.vaska80s.samples.resizer.Resizer;

import java.io.*;

/**
 * Main class
 */
public class App {
    private final static Logger log = Logger.getLogger(App.class);

    private final static int OUT_WIDTH = 640;
    private final static int OUT_HEIGHT = 640;

    private static String dropBoxToken = "GI6iMyo3QLAAAAAAAAAACPBgOpAMc-NO7lmSBC4L1rpZUz_7apqexoIUhWFNKU5Z";

    public static void main(String[] args) {
        try {
            upload(new File("../he-he.jpg"));

        } catch (DbxException | IOException e) {
            log.error(e);
        }
    }

    private static void upload(File fileToUpload) throws DbxException, IOException {
        DbxRequestConfig config = new DbxRequestConfig("uploaderBot/1.0");
        DbxClientV2 client = new DbxClientV2(config, dropBoxToken);

        FullAccount account = client.users().getCurrentAccount();
        System.out.println("Using account: " + account.getName().getDisplayName());

        try (InputStream in = new FileInputStream(fileToUpload)) {
            FileMetadata metadata = client.files().uploadBuilder("/" + fileToUpload.getName()).uploadAndFinish(in);
        }

    }

    private static void resize(){
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
