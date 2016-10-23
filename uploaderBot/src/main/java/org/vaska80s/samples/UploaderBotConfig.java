package org.vaska80s.samples;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Vasiliy Serov.
 */
class UploaderBotConfig {

    private Properties prop;

    UploaderBotConfig() throws IOException {
        prop = new Properties();

        String propFileName = "uploadbot.properties";
        InputStream inputStream = new FileInputStream(propFileName);
        prop.load(inputStream);
    }

    String getBrokerHost() {
        return prop.getProperty("queue.rabbitmq.host", "localhost");
    }

    String getResizerDstDir() {
        return prop.getProperty("resizer.tmpdir", "./");
    }

    String getDropboxToken() {
        return prop.getProperty("uploader.dropboxtoken", "");
    }
}
