package org.vaska80s.samples;


import com.drew.imaging.ImageProcessingException;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.users.FullAccount;
import org.apache.log4j.Logger;
import org.vaska80s.samples.agents.Resizer;
import org.vaska80s.samples.agents.Scheduler;

import java.io.*;
import java.util.concurrent.TimeoutException;

/**
 * Main class
 */
public class App {
    private final static Logger log = Logger.getLogger(App.class);

    private final static int OUT_WIDTH = 640;
    private final static int OUT_HEIGHT = 640;

    public static void main(String[] args) {
        if(args.length == 0) {
            printUsage();
            System.exit(0);
        }

        String command = args[0];
        switch (command){
            case "schedule":
                if(args.length < 2){
                    System.out.println("You have to specify the source directory");
                    break;
                }
                String srcDirectory = args[1];
                try {
                    Scheduler scheduler = new Scheduler(srcDirectory);
                    scheduler.schedule();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                } catch (TimeoutException e) {
                    System.out.println("FATAL: an error occured communicating with queue:");
                    System.out.println(e.getMessage());
                }
                break;
            default:
                printUsage();
        }
//        try (QueueManager queueManager = new QueueManager(QueueManager.RESIZER_Q_NAME)) {
//
//            if ("send".equals(args[0])) {
//                queueManager.pushUrl("../27388.jpg");
//                queueManager.pushUrl("../RM.png");
//                queueManager.pushUrl("../fit.jpg");
//                queueManager.pushUrl("../20160622_222108.jpg");
//                queueManager.pushUrl("../20160622_222108.png");
//            } else if ("count".equals(args[0])) {
//                System.out.println(queueManager.getCount());
//            } else if ("read".equals(args[0])) {
//                System.out.println(queueManager.getNextUrl());
//            }
//
//        } catch (Exception e) {
//            log.error(e, e);
//        }
    }

    private static void upload() throws DbxException, IOException {
        File fileToUpload = new File("../he-he.jpg");
        DbxRequestConfig config = new DbxRequestConfig("uploaderBot/1.0");
        String dropBoxToken = "GI6iMyo3QLAAAAAAAAAACPBgOpAMc-NO7lmSBC4L1rpZUz_7apqexoIUhWFNKU5Z";
        DbxClientV2 client = new DbxClientV2(config, dropBoxToken);

        FullAccount account = client.users().getCurrentAccount();
        System.out.println("Using account: " + account.getName().getDisplayName());

        try (InputStream in = new FileInputStream(fileToUpload)) {
            FileMetadata metadata = client.files().uploadBuilder("/" + fileToUpload.getName()).uploadAndFinish(in);
        }

    }

    private static void resize() {
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

    private static void printUsage(){
        System.out.println("Uploader Bot");
        System.out.println("Usage:");
        System.out.println("\tcommand [arguments]");
        System.out.println("Available commands:");
        System.out.println("\tschedule\tAdd filenames to resize queue");
        System.out.println("\tresize\t\tResize next images from the queue");
        System.out.println("\tstatus\t\tOutput current status in format %queue%:%number_of_images%");
        System.out.println("\tupload\t\tUpload next images to remote storage");
    }
}
