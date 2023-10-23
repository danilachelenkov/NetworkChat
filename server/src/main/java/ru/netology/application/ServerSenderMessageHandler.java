package ru.netology.application;

import ru.netology.Message;
import ru.netology.MessageType;
import ru.netology.logger.Writable;
import ru.netology.model.User;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Set;

public class ServerSenderMessageHandler implements Runnable {
    private final String className = getClass().getName();
    private final String threadName = Thread.currentThread().getName();
    private final Writable loggerThread;
    private final Map<Integer, User> usersChatMap;
    Message message = null;

    public ServerSenderMessageHandler(Writable logger) {
        loggerThread = logger;
        this.usersChatMap = Server.getUsersChatMap();
    }

    @Override
    public void run() {

        loggerThread.debug(prepare("Запущен поток обработки очереди сообщений"));

        while (true) {
            try {
                message = Server.messageQueue.take();
                loggerThread.debug(prepare(String.format("Получено новое сообщение от [%s] с текстом: {%s}", message.getLogin(), message.getTextMessage())));

            } catch (InterruptedException ex) {
                loggerThread.error(prepare("Произошло прерывание потока обработки очереди: " + ex.getMessage()));
                return;
            }

            if (message.getMessageType().equals(MessageType.TO_USER)) {

                User usr = getUserBySocketPort(message.getUniqSessionId());
                sendMessage(usr.getSocket(), message);

                loggerThread.debug(prepare(String.format("Отправка серверного сообщения [%s] Пользователю [%s]",
                        message.getTextMessage(),
                        usr.getLogin())));
            }

            for (Map.Entry<Integer, User> entity : getUsersSet()) {
                if (!entity.getKey().equals(message.getUniqSessionId())) {
                    loggerThread.debug(prepare(String.format("Выполняется отправка широковещательного сообщения для Пользователя [%s]", entity.getValue().getLogin())));

                    sendMessage(entity.getValue().getSocket(), message);
                    loggerThread.debug(prepare(String.format("Пользователю [%s] отправлено широковещательного сообшение [%s]", entity.getValue().getLogin(), message.getTextMessage())));
                }
            }

        }
    }

    private void sendMessage(Socket socketClient, Message message) {
        try {
            new ObjectOutputStream(socketClient.getOutputStream())
                    .writeObject(message);
        } catch (IOException ex) {
            loggerThread.error("Ошибка отправки сообщения в Socket Client:\n" + ex.getMessage());
        }
    }

    public User getUserBySocketPort(Integer userSocket) {
        synchronized (usersChatMap) {
            return usersChatMap.get(userSocket);
        }
    }

    private Set<Map.Entry<Integer, User>> getUsersSet() {
        synchronized (usersChatMap) {
            return usersChatMap.entrySet();
        }
    }

    private String prepare(String msg) {
        return String.format("[%s] [%s] %s",
                threadName,
                className,
                msg);
    }
}
