package ru.netology.application;

import ru.netology.Message;
import ru.netology.logger.Writable;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private static final String className = Client.class.getName();
    private static final String threadName = Thread.currentThread().getName();
    private String login = "nologin";
    private boolean isLoadError = false;
    private ClientSettings clientSettings;
    private Writable logger;
    private Printable printer;
    private final ExecutorService executorReadService = Executors.newFixedThreadPool(1);
    ;
    private Socket socket = null;
    private ObjectOutputStream objectOutputStream;

    public Client(Writable baseLogger) {
        logger = baseLogger;
        printer = new PrinterMessage();

        logger.debug(prepare("Старт инициализации параметро клиента"));
        clientSettings = new ClientSettings("client.properties", logger);

        if (clientSettings.isLoadError()) {
            logger.error(prepare("Ошибка загрузки настроек. Клиент не может быть запущен"));
            return;
        }

        logger.debug(prepare(String.format("Параметры инициализированны [%s]", clientSettings.toString())));
    }

    public void start() {
        logger.debug(prepare("Клиент запущен"));

        if (isLoadError) {
            logger.error(prepare("Ошибка загрузки параметров настройки клиента. Работа клиент завершена"));
            return;
        }

        try {
            logger.debug(prepare("Выполняется создание Socket  и подключение к Серверу по инициализированным настройкам"));
            socket = new Socket(clientSettings.getServerIpAdress(), clientSettings.getServerPort());
            logger.debug(prepare("Подключение к Серверу выполнено успешно"));

            logger.debug(prepare("Запускается поток для обработки получения ответных сообщений от Сервера"));
            executorReadService.execute(new ClientReceiveHandler(socket, new PrinterMessage(), logger));

            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            Scanner scanner = new Scanner(System.in);

            while (!socket.isClosed()) {
                if (scanner.hasNext()) {
                    String line = scanner.nextLine();

                    logger.debug(prepare(String.format("Выполняется попытка отправки пользовательского сообщения" +
                            "с текстом {%s} на Сервер", line))
                    );
                    sendMessage(generateMessage(line));

                    if (line.contains("/reg") && login.equals("nologin")) {
                        logger.debug(prepare(String.format("Отправлено техническое сообщение для регистрации на сервере", line)));

                        if (line.split("/")[0].trim().length() > 0) {
                            login = line.split("/")[0].trim();
                        } else {
                            logger.warn(prepare("Пользователь передал пустое имя пользователя в техническом сообщении"));
                        }
                    }

                    if (line.contains("/exit")) {
                        logger.warn(prepare("Отправлено техническое сообщение для выхода из чата"));
                        printer.print("Выход из чата выполнен. Клиент завершает работу");
                        
                        socket.close();
                        executorReadService.shutdown();
                    }
                }
            }
        } catch (IOException e) {
            logger.error(prepare("Ошибка при обработке действий Пользователя:\n" + e.getMessage()));
        }
        printer.print("Работа программы завершена. До встречи!");
        logger.debug(prepare("Работа программы завершена."));
    }

    private void sendMessage(Message message) {
        try {
            objectOutputStream.writeObject(message);
            logger.debug(prepare(String.format("Сообщение с текстом {%s} успешно отправлено", message.getTextMessage())));
        } catch (IOException ex) {
            logger.error(prepare("Ошибка отправки сообщения:.\n " + ex.getMessage()));
        }

    }

    private Message generateMessage(String msg) {
        logger.debug(prepare(String.format("Создание объекта сообщения Message. Текст сообщения: {%s}", msg)));
        return new Message(login, msg, socket.getLocalPort());
    }

    public static String prepare(String msg) {
        return String.format("[%s] [%s] %s",
                threadName,
                className,
                msg);
    }
}
