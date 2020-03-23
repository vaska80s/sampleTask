package org.vaska80s.samples.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.vaska80s.samples.QueueException;
import org.vaska80s.samples.UploaderBotConfig;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Vasiliy Serov.
 */
public class QueueManager {
    public static final String RESIZER_Q_NAME = "resizer";
    public static final String UPLOAD_Q_NAME = "upload";
    public static final String DONE_Q_NAME = "done";
    public static final String FAIL_Q_NAME = "fail";

    private Connection connection;
    private Channel channel;
    private AMQP.Queue.DeclareOk declareOk;
    private String queueName;

    /**
     * Constructor
     *
     * @param queueName name of queue
     * @throws QueueException if can't initialize the queue
     */
    public QueueManager(String queueName, UploaderBotConfig config) throws QueueException {
        ConnectionFactory factory = new ConnectionFactory();
        try {
            factory.setHost(config.getBrokerHost());
            factory.setUsername(config.getRabbitUser());
            factory.setPassword(config.getRabbitPassword());
            connection = factory.newConnection();
            channel = connection.createChannel();
            declareOk = channel.queueDeclare(queueName, false, false, false, null);
        } catch (IOException | TimeoutException e) {
            throw new QueueException(e);
        }

        this.queueName = queueName;
    }

    /**
     * Put image info to queue
     *
     * @param message message object to put
     * @throws QueueException if error occured
     */
    public void pushMsg(Message message) throws QueueException {
        try {
            channel.basicPublish("", queueName, null, new ObjectMapper().writeValueAsBytes(message));
        } catch (IOException e) {
            throw new QueueException(e);
        }
    }

    /**
     * Get url from queue
     *
     * @return url or null if queue is empty
     * @throws QueueException if error occured
     */
    public Message getNextMsg() throws QueueException {
        Message result = null;
        try {
            GetResponse response = channel.basicGet(queueName, false);
            if (response != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                result = objectMapper.readValue(response.getBody(), Message.class);
                result.setMessageTag(response.getEnvelope().getDeliveryTag());
            }
        } catch (IOException e) {
            throw new QueueException(e);
        }

        return result;
    }

    /**
     * Acknoledge that message processed
     *
     * @param message message
     */
    public void msgAck(Message message) {
        try {
            channel.basicAck(message.getMessageTag(), false);
        } catch (IOException e) {
            System.out.println("Failed to operate with queue");
        }
    }

    /**
     * Inform the queue that message processing failed
     *
     * @param message message
     */
    public void msgNack(Message message) {
        try {
            channel.basicNack(message.getMessageTag(), false, true);
        } catch (IOException e) {
            System.out.println("Failed to operate with queue");
        }
    }

    /**
     * Get number of items in queue
     *
     * @return number of items
     */
    public int getCount() {
        return declareOk.getMessageCount();
    }

    public void close() {
        try {
            if (channel != null) {
                channel.close();
            }

            if (connection != null) {
                connection.close();
            }
        } catch (IOException | TimeoutException e) {
            System.out.println("Failed to close queue");
        }
    }
}
