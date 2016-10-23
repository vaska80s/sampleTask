package org.vaska80s.samples.queue;

import com.rabbitmq.client.*;
import org.vaska80s.samples.UploaderBotConfig;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Vasiliy Serov.
 */
public class QueueManager implements AutoCloseable {
    public static final String RESIZER_Q_NAME = "resizer";
    public static final String UPLOAD_Q_NAME = "upload";
    public static final String DONE_Q_NAME = "done";
    public static final String FAIL_Q_NAME = "fail";

    private Connection connection = null;
    private Channel channel = null;
    private AMQP.Queue.DeclareOk declareOk;
    private String queueName;

    public QueueManager(String queueName) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(UploaderBotConfig.getInstance().getBrokerHost());
        connection = factory.newConnection();
        channel = connection.createChannel();
        declareOk = channel.queueDeclare(queueName, false, false, false, null);
        this.queueName = queueName;
    }

    public void pushUrl(String url) throws IOException {
        channel.basicPublish("", queueName, null, url.getBytes());
    }

    public String getNextUrl() throws IOException {
        GetResponse response = channel.basicGet(queueName, true);
        String result = null;
        if(response != null) {
            result = new String(response.getBody(), "UTF-8");
        }

        return result;
    }

    public int getCount() throws IOException {
        return declareOk.getMessageCount();
    }

    @Override
    public void close() throws IOException, TimeoutException {
        if (channel != null){
            channel.close();
        }

        if(connection != null){
            connection.close();
        }
    }
}
