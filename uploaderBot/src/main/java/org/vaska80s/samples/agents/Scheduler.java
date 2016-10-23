package org.vaska80s.samples.agents;

import org.vaska80s.samples.QueueException;
import org.vaska80s.samples.queue.Message;
import org.vaska80s.samples.queue.QueueManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Vasiliy Serov.
 */
public class Scheduler {
    private File imageDirectory;

    /**
     * Constructor
     * @param strDirectory source directory with files to process. Directory must exist and have permission to read
     *                     and write
     * @throws IOException if specified path is not a directory or user has insufficient privileges
     * @throws FileNotFoundException if specified path does not exist
     */
    public Scheduler(String strDirectory) throws IOException {
        imageDirectory = new File(strDirectory);
        if (!imageDirectory.exists()) {
            throw new FileNotFoundException("Source directory does not exist (" + imageDirectory.getCanonicalPath() + ")");
        }

        if(!imageDirectory.isDirectory()) {
            throw new IOException("Given path is not a directory (" + imageDirectory.getCanonicalPath() + ")");
        }

        if(!imageDirectory.canRead() || !imageDirectory.canWrite()) {
            throw new IOException("Lack of access to source directory (read or write)");
        }
    }

    /**
     * Schedule files for further processing
     *
     * @throws QueueException communication problem with a queue
     */
    public void schedule(QueueManager queue) throws QueueException {
        try{
            File[] files = imageDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    Message message = new Message(file.getCanonicalPath());
                    queue.pushMsg(message);
                }
            }
        } catch (IOException e) {
            System.out.println("Can't get path for file");
        }
    }
}

