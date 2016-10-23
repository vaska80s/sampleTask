package org.vaska80s.samples;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Vasiliy Serov.
 */
public class UploaderBotConfig {

    private static UploaderBotConfig ourInstance;

    private Properties prop;

    private UploaderBotConfig() throws IOException {
        prop = new Properties();

        String propFileName = "/uploadbot.properties";
        InputStream inputStream = getClass().getResourceAsStream(propFileName);

        if(inputStream != null){
            prop.load(inputStream);
        } else {
            throw new FileNotFoundException("Configuration file " + propFileName + " not found in the classpath");
        }
    }

    public static UploaderBotConfig getInstance() throws IOException {
        if(ourInstance == null) {
            ourInstance = new UploaderBotConfig();
        }

        return ourInstance;
    }

    public String getBrokerHost(){
        return prop.getProperty("queue.rabbitmq.host", "localhost");
    }

    public String getResizerDstDir() {
        return prop.getProperty("resizer.tmpdir");
    }
}
