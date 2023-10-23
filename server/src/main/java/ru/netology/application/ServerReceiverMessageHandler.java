package ru.netology.application;

import ru.netology.Message;
import ru.netology.MessageType;
import ru.netology.logger.Writable;
import ru.netology.model.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class ServerReceiverMessageHandler implements Runnable {
    private final String className = getClass().getName();
    private final String threadName = Thread.currentThread().getName();

    private final Integer keyUserValue;
    private final Socket socketClientIn;
    private final Writable loggerThread;
    private User user;
    private boolean isChatUser = false;
    private ObjectInputStream receivedObjFromClient;
    private Map<Integer, User> mapUsers;

    public ServerReceiverMessageHandler(Socket socket, Writable logger) {
        socketClientIn = socket;
        loggerThread = logger;
        keyUserValue = socket.getPort();

        logger.debug(prepare("Класс инициализирован с Socket: " + socket.getPort()));
    }

    @Override
    public void run() {
        loggerThread.debug(prepare(String.format("Запущен поток обработки Socket= %s", keyUserValue)));

        try {
            receivedObjFromClient = new ObjectInputStream(socketClientIn.getInputStream());
        } catch (IOException e) {
            loggerThread.error(prepare("Ошибка в работе SocketStream:\n" + e.getMessage()));
        }

        while (!socketClientIn.isClosed()) {
            loggerThread.debug(prepare("Socket активен"));

            mapUsers = Server.getUsersChatMap();

            try {
                Message message = (Message) receivedObjFromClient.readObject();

                loggerThread.debug(prepare("Сервер получил сообщение: " + message.getTextMessage()));

                if (!isChatUser) {
                    loggerThread.debug(prepare("Пользователь отправивший сообщение не является пользователем чата"));

                    if (message.getTextMessage().toLowerCase().contains("/reg")) {

                        loggerThread.debug(prepare("От пользователя получено сообщение о регистрации: " + message.getTextMessage()));

                        String userName = message.getTextMessage().split("/")[0].trim();
                        if (userName.length() > 0) {
                            user = new User(
                                    message.getTextMessage().split("/")[0].trim(),
                                    socketClientIn
                            );
                        } else {
                            sendClientTechnicalMessage("Имя пользователя не может быть пустым. Введите имя и пройдите регистрацию");
                            loggerThread.warn(prepare(String.format("Пользователь Socket[%s] при попытке регистрации не заполнил имя пользователя",keyUserValue)));
                            continue;
                        }

                        loggerThread.debug(prepare("Инициализирован пользователь: " + user.toString()));

                        synchronized (mapUsers) {
                            mapUsers.put(keyUserValue, user);
                        }

                        isChatUser = true;

                        loggerThread.debug(prepare("Всего пользователей в списке: " + Server.getUsersChatMap().size()));
                        loggerThread.debug(prepare("Логин: " + user.getLogin() + " добавлен в список пользователей чата"));

                        putMessageToQueue(
                                createServerMessage(
                                        "Вы добавлены как пользователь в сетевой чат" +
                                                " желаем приятного общения! Если захотите покинуть чат введите команду /exit"
                                )
                        );
                    } else {
                        loggerThread.debug(prepare("Отправка уведомления клиенту что он не зарегистрирован в списке пользователей чата "
                                + keyUserValue));

                        sendClientTechnicalMessage("Вы подключились к сетевому чату. " +
                                " Для того, чтобы начать общаться, необходимо пройти регистрацию на сервере: " +
                                "введите свой <Login>, а после команду /reg. Например: <Login>/reg");
                    }
                } else {
                    try {

                        if (message.getTextMessage().toLowerCase().contains("/reg")) {
                            sendClientTechnicalMessage("Имя пользователя не может быть изменено, если хотите изменить имя" +
                                    " покиньте чат и авторизуйтесь заново.");
                            loggerThread.warn(prepare(String.format("Пользователь [%s] пытался сменить имя. Пользователю направлено информационное сообщение", user.getLogin())));
                            continue;
                        }

                        if (message.getTextMessage().toLowerCase().contains("/exit")) {
                            loggerThread.warn(prepare("Пользователь отправил команду для выхода из чата"));
                            sendGoodBuyAndSyncUsersList(isChatUser);
                            loggerThread.warn(prepare(String.format("Пользователей осталось в чате {%s}", mapUsers.size())));
                            break;
                        }

                        if (mapUsers.size() == 1) {
                            putMessageToQueue(
                                    createServerMessage(
                                            String.format("Уважаемый пользователь [%s] на данный момент в чате вы в одиночесте", user.getLogin())
                                    )
                            );

                            loggerThread.debug(prepare(String.format("Пользователю [%s] отправлено сообщения одиночества.", user.getLogin())));
                        }

                        message.setLogin(user.getLogin());
                        putMessageToQueue(message);

                        loggerThread.debug(prepare("Добавлено сообщение в очередь для рассылки всем участникам чата"));
                        loggerThread.debug(prepare("Размер очереди: " + Server.messageQueue.size()));

                    } catch (Exception ex) {
                        loggerThread.error(prepare("Ошибка при обработке действий зарегистрированного пользователя:\n" + ex.getMessage()));
                    }
                }
            } catch (Exception ex) {
                sendGoodBuyAndSyncUsersList(isChatUser);

                loggerThread.error(prepare("Ошибка:\n" + ex.getMessage()));
                loggerThread.debug(prepare(String.format("Пользователь сокета [%s] покинул сервер по неизвестным причинам. Socket утерян", keyUserValue)));
                loggerThread.warn(prepare(String.format("Пользователей осталось в чате {%s}",
                        mapUsers.size()))
                );

                loggerThread.debug(prepare("Socket клиента закрыт. Завершаем текущий поток обработки."));
                loggerThread.warn(prepare("Поток завершен."));
                return;
            }
        }

        loggerThread.warn(prepare("Поток завершился"));
    }

    private void sendGoodBuyAndSyncUsersList(boolean registered) {
        if (registered) {
            synchronized (mapUsers) {
                if (mapUsers.get(keyUserValue) != null) {
                    mapUsers.remove(keyUserValue);
                }
            }

            putMessageToQueue(
                    new Message(Server.SERVER_LOGIN,
                            String.format("Пользователь [%s] покинул чат", user.getLogin()),
                            keyUserValue)
            );
        }
    }

    private void putMessageToQueue(Message message) {
        try {
            Server.messageQueue.put(message);
        } catch (InterruptedException ex) {
            loggerThread.error(prepare("Произошло прерываение потока:\n" + ex.getMessage()));
        }
    }

    private void sendClientTechnicalMessage(String msg) {
        try {
            new ObjectOutputStream(socketClientIn.getOutputStream())
                    .writeObject(createServerMessage(msg));
        } catch (IOException ex) {
            loggerThread.error(prepare("Ошибка отправки серверного сообщения: " + ex.getMessage()));
        }

    }

    public Message createServerMessage(String msgText) {
        return new Message(
                MessageType.TO_USER,
                Server.SERVER_LOGIN,
                msgText,
                keyUserValue);
    }

    public String prepare(String msg) {
        return String.format("[%s] [%s] [Socket=%s] %s",
                threadName,
                className,
                keyUserValue.toString(),
                msg);
    }

}
