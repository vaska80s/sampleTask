package org.vaska80s.samples.agents;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

import java.io.*;

/**
 * @author Vasiliy Serov.
 */
public class Uploader {

    private DbxClientV2 client;

    /**
     * Constructor
     *
     * @param accessToken authorization token for Dropbox
     */
    public Uploader(String accessToken) {
        DbxRequestConfig config = new DbxRequestConfig("uploaderBot/1.0");
        client = new DbxClientV2(config, accessToken);

        FullAccount account;
        try {
            account = client.users().getCurrentAccount();
            System.out.println("Using account: " + account.getName().getDisplayName());
        } catch (DbxException e) {
            System.out.println("Can't get information about account: " + e.getMessage());
        }
    }

    /**
     * Upload file to Dropbox
     * @param filePath filepath to upload
     */
    public void upload(String filePath) throws IOException, DbxException {
        File fileToUpload = new File(filePath);

        try (InputStream in = new FileInputStream(fileToUpload)) {
            client.files().uploadBuilder("/" + fileToUpload.getName()).uploadAndFinish(in);
        }
    }
}
