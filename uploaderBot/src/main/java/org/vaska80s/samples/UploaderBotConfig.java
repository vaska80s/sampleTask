package org.vaska80s.samples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Vasiliy Serov.
 */
public class UploaderBotConfig {

    private Properties prop;

    UploaderBotConfig() throws IOException {
        prop = new Properties();

        File propFile = new File("uploadbot.properties");
        InputStream inputStream = new FileInputStream(propFile);
        prop.load(inputStream);
    }

    public String getBrokerHost() {
        return prop.getProperty("queue.rabbitmq.host", "localhost");
    }

    public String getResizerDstDir() {
        return prop.getProperty("resizer.tmpdir", "./");
    }

    public String getDropboxToken() {
        return prop.getProperty("uploader.dropboxtoken", "");
    }

    public String getRabbitUser(){
        return prop.getProperty("rabbit.user", "guest");
    }

    public String getRabbitPassword(){
        return prop.getProperty("rabbit.password", "guest");
    }
}
