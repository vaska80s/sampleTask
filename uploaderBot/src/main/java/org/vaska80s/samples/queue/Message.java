package org.vaska80s.samples.queue;


/**
 * @author Vasiliy Serov.
 */
public class Message {
    private String sourceUrl;
    private String resizedUrl;
    private long messageTag;

    public Message() {}

    public Message(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    Message(String sourceUrl, String resizedUrl, long messageTag) {
        this.sourceUrl = sourceUrl;
        this.resizedUrl = resizedUrl;
        this.messageTag = messageTag;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    long getMessageTag() {
        return messageTag;
    }

    public String getResizedUrl() {
        return resizedUrl;
    }

    public void setResizedUrl(String resizedUrl) {
        this.resizedUrl = resizedUrl;
    }
}
