package ru.netology;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    private String login;
    private final String text;
    private final Date datetime;
    private final Integer uniqSessionId;
    private final MessageType messageType;

    public Message(String login, String text, int uniqSessionId) {
        this.login = login;
        this.text = text;
        this.uniqSessionId = uniqSessionId;
        this.datetime = new Date(System.currentTimeMillis());
        this.messageType = MessageType.OTHER_USERS;

    }

    public Message(MessageType messageType, String login, String text, int uniqSessionId) {
        this.login = login;
        this.text = text;
        this.uniqSessionId = uniqSessionId;
        this.datetime = new Date(System.currentTimeMillis());

        this.messageType = messageType;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public String getTextMessage() {
        return text;
    }

    public Integer getUniqSessionId() {
        return uniqSessionId;
    }

    public Date getDatetime() {
        return datetime;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public String toString() {
        return "Message{" +
                "login='" + login + '\'' +
                ", message='" + text + '\'' +
                ", datetime=" + datetime +
                ", uniqSessionId=" + uniqSessionId +
                ", messageType=" + messageType +
                '}';
    }
}
