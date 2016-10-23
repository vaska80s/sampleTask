package org.vaska80s.samples;


import com.drew.imaging.ImageProcessingException;
import com.dropbox.core.DbxException;
import org.vaska80s.samples.agents.Resizer;
import org.vaska80s.samples.agents.Scheduler;
import org.vaska80s.samples.agents.Uploader;
import org.vaska80s.samples.queue.Message;
import org.vaska80s.samples.queue.QueueManager;

import java.io.*;

/**
 * Main class
 */
public class App {

    private QueueManager resizerQueue = null;
    private QueueManager uploaderQueue = null;
    private QueueManager doneQueue = null;
    private QueueManager failQueue = null;

    private UploaderBotConfig config;

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(0);
        }

        App app = null;
        try {
            app = new App();
            String command = args[0];

            switch (command) {
                case "schedule":
                    if (args.length < 2) {
                        System.out.println("You have to specify the source directory");
                        break;
                    }

                    app.schedule(args[1]);

                    break;
                case "resize":
                    Integer count = getCount(args);
                    if (count == null) {
                        break;
                    }

                    app.resize(count);

                    break;
                case "upload":
                    count = getCount(args);
                    if (count == null) {
                        break;
                    }

                    app.upload(count);

                    break;
                case "retry":
                    count = getCount(args);
                    if (count == null) {
                        break;
                    }

                    app.reschedule(count);

                    break;
                case "status":
                    app.printStatus();

                    break;
                default:
                    printUsage();
            }
        } catch (IOException e) {
            System.out.println("Can't read configuration file");
        } finally {
            if (app != null) {
                app.closeQueues();
            }
        }
    }


    private App() throws IOException {
        config = new UploaderBotConfig();
    }


    private static void printUsage() {
        System.out.println("Uploader Bot");
        System.out.println("Usage:");
        System.out.println("\tcommand [arguments]");
        System.out.println("Available commands:");
        System.out.println("\tschedule\tAdd filenames to resize queue");
        System.out.println("\tresize\t\tResize next images from the queue");
        System.out.println("\tstatus\t\tOutput current status in format %queue%:%number_of_images%");
        System.out.println("\tupload\t\tUpload next images to remote storage");
        System.out.println("\tretry\t\tReschedule failed images");
    }


    private void schedule(String srcDirectory) {
        try {
            resizerQueue = new QueueManager(QueueManager.RESIZER_Q_NAME, config.getBrokerHost());
            Scheduler scheduler = new Scheduler(srcDirectory);
            scheduler.schedule(resizerQueue);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (QueueException e) {
            System.out.println("FATAL: an error occured communicating with queue:");
            System.out.println(e.getMessage());
        }
    }


    private void resize(int count) {
        try {
            resizerQueue = new QueueManager(QueueManager.RESIZER_Q_NAME, config.getBrokerHost());
            uploaderQueue = new QueueManager(QueueManager.UPLOAD_Q_NAME, config.getBrokerHost());
            failQueue = new QueueManager(QueueManager.FAIL_Q_NAME, config.getBrokerHost());
        } catch (QueueException e) {
            System.out.println("FATAL: can't initialize queue: " + e.getMessage());
            return;
        }

        Resizer resizer = new Resizer(640, 640);

        try {
            Message message = resizerQueue.getNextMsg();
            while (count != 0 && message != null) {
                File fileToProcess = new File(message.getSourceUrl());
                File tmpDir = new File(config.getResizerDstDir());
                File resultFile = new File(tmpDir, fileToProcess.getName());
                try {
                    resizer.resize(fileToProcess, resultFile);
                    message.setResizedUrl(resultFile.getCanonicalPath());
                    if (fileToProcess.delete()) {
                        moveMsgTo(resizerQueue, uploaderQueue, message);
                    } else {
                        moveMsgTo(resizerQueue, failQueue, message);
                    }
                } catch (IOException | ImageProcessingException e) {
                    System.out.println("Can't resize " + message.getSourceUrl() +
                            ". Moving item to fail queue, you can try later. " + e.getMessage());
                    moveMsgTo(resizerQueue, failQueue, message);
                }
                message = resizerQueue.getNextMsg();
                if(count > -1){
                    --count;
                }
            }
        } catch (QueueException e) {
            System.out.println("FATAL: an error occured communicating with queue:");
            System.out.println(e.getMessage());
        }
    }


    private void upload(Integer count) {
        try {
            uploaderQueue = new QueueManager(QueueManager.UPLOAD_Q_NAME, config.getBrokerHost());
            doneQueue = new QueueManager(QueueManager.DONE_Q_NAME, config.getBrokerHost());
            failQueue = new QueueManager(QueueManager.FAIL_Q_NAME, config.getBrokerHost());
        } catch (QueueException e) {
            System.out.println("FATAL: can't initialize queue: " + e.getMessage());
            return;
        }

        Uploader uploader = new Uploader(config.getDropboxToken());

        try {
            Message message = uploaderQueue.getNextMsg();
            while (count != 0 && message != null) {
                try {
                    if(message.getResizedUrl() != null) {
                        uploader.upload(message.getResizedUrl());
                        moveMsgTo(uploaderQueue, doneQueue, message);
                    } else {
                        System.out.println("Invalid message - no resized url. Moving to fail queue");
                        moveMsgTo(uploaderQueue, failQueue, message);
                    }
                } catch (IOException | DbxException e) {
                    System.out.println("Can't upload " + message.getResizedUrl() +
                            ". Moving item to fail queue, you can try later. " + e.getMessage());
                    moveMsgTo(uploaderQueue, failQueue, message);
                }
                message = uploaderQueue.getNextMsg();
                if(count > -1){
                    --count;
                }
            }
        } catch (QueueException e) {
            System.out.println("FATAL: an error occured communicating with queue:");
            System.out.println(e.getMessage());
        }
    }

    private void reschedule(int count) {
        try {
            resizerQueue = new QueueManager(QueueManager.RESIZER_Q_NAME, config.getBrokerHost());
            failQueue = new QueueManager(QueueManager.FAIL_Q_NAME, config.getBrokerHost());
        } catch (QueueException e) {
            System.out.println("FATAL: can't initialize queue: " + e.getMessage());
            return;
        }

        try {
            Message message = failQueue.getNextMsg();
            while (count != 0 && message != null) {
                message.setResizedUrl(null);
                moveMsgTo(failQueue, resizerQueue, message);
                message = failQueue.getNextMsg();
                if(count > -1){
                    --count;
                }
            }
        } catch (QueueException e) {
            System.out.println("FATAL: an error occured communicating with queue:");
            System.out.println(e.getMessage());
        }
    }

    private void printStatus(){
        try {
            resizerQueue = new QueueManager(QueueManager.RESIZER_Q_NAME, config.getBrokerHost());
            uploaderQueue = new QueueManager(QueueManager.UPLOAD_Q_NAME, config.getBrokerHost());
            doneQueue = new QueueManager(QueueManager.DONE_Q_NAME, config.getBrokerHost());
            failQueue = new QueueManager(QueueManager.FAIL_Q_NAME, config.getBrokerHost());
        } catch (QueueException e) {
            System.out.println("FATAL: can't initialize queue: " + e.getMessage());
            return;
        }

        System.out.println("Images Processor Bot");
        System.out.println("Queue\tCount");
        System.out.println("resize\t" + resizerQueue.getCount());
        System.out.println("upload\t" + uploaderQueue.getCount());
        System.out.println("done\t" + doneQueue.getCount());
        System.out.println("failed\t" + failQueue.getCount());
    }

    private void moveMsgTo(QueueManager sourceQueue, QueueManager destQueue, Message message) {
        try {
            destQueue.pushMsg(message);
            sourceQueue.msgAck(message);
        } catch (QueueException e) {
            System.out.println("Failed to operate with queue");
            sourceQueue.msgNack(message);
        }
    }


    /**
     * Get count of items to be processed
     *
     * @param args command arguments
     * @return count of items to process, -1 if not specified in command arguments, null if incorrect format given
     */
    private static Integer getCount(String[] args) {
        if (args.length < 2) {
            return -1;
        }

        if (!"-n".equals(args[1])) {
            System.out.println(args[1] + " not understood");
            return null;
        }

        if (args.length < 3 || !args[2].matches("[0-9]+")) {
            System.out.println("Please specify a number of items to process");
            return null;
        }

        return Integer.parseInt(args[2]);
    }


    private void closeQueues() {
        if (resizerQueue != null) {
            resizerQueue.close();
        }

        if (uploaderQueue != null) {
            uploaderQueue.close();
        }

        if (doneQueue != null) {
            doneQueue.close();
        }

        if (failQueue != null) {
            failQueue.close();
        }
    }
}
