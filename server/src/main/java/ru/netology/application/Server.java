package ru.netology.application;

import ru.netology.Message;
import ru.netology.logger.Writable;
import ru.netology.model.User;

import java.io.*;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static final String SERVER_LOGIN = "SERVER";
    private final String className = getClass().getName();
    private final String threadName = Thread.currentThread().getName();
    private static Integer serverPort;
    private static String serverIp;
    private static Integer maxClientCount;
    private static Integer maxMessageQueue;
    private final Writable logger;
    private static Map<Integer, User> usersChatMap;
    public static ArrayBlockingQueue<Message> messageQueue = null;

    private ExecutorService executorWaitClientService;
    private ExecutorService executorSender;

    public Server(Writable logger) {

        this.logger = logger;
        usersChatMap = new HashMap<>();

        try {
            logger.serv(prepare("Выполняется запуск cервера..."));
            logger.debug(prepare("Инициализация параметров Сервера"));

            loadServerConfiguration();

            messageQueue = new ArrayBlockingQueue<>(maxMessageQueue);
            executorSender = Executors.newFixedThreadPool(1);
            executorWaitClientService = Executors.newFixedThreadPool(1);

            logger.debug(prepare("Сервер запущен. Инциализация прошла успешно"));
        } catch (Exception ex) {
            logger.error(prepare(String.join(" ", "Ошибка инициализации параметров:\n", ex.getMessage())));
        }
    }

    public void start() {
        logger.debug(prepare("Запуск потока очереди ожидания подключения и наполнения очереди сообщений от пользователе чата"));
        executorWaitClientService.execute(new ServerWaitClientHandler(logger, serverPort, maxClientCount));

        logger.debug(prepare("Запуск потока обработки очереди сообщений"));
        executorSender.execute(new ServerSenderMessageHandler(logger));
    }

    //В модуле Client сделал инициализацию через экземпляр класса отвечающего за инициализацию параметров
    private void loadServerConfiguration() {

        Properties properties = new Properties();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("server.properties");

        try {
            logger.debug(prepare("Загрузка настроек"));
            properties.load(inputStream);

            if (properties.getProperty("port").equals("")) {
                logger.error("Порт сервера не определен в настроечном файле сервера. Запуск сервера невозможен");
                return;
            } else {
                serverPort = Integer.parseInt(properties.getProperty("port"));
            }

            if (properties.getProperty("client.max.count").equals("")) {
                maxClientCount = 5;
            } else {
                maxClientCount = Integer.parseInt(properties.getProperty("client.max.count"));
            }

            if (properties.getProperty("queue.message.max.count").equals("")) {
                maxMessageQueue = Integer.parseInt(properties.getProperty("queue.message.max.count"));
            } else {
                maxMessageQueue = 50;
            }
        } catch (IOException | NumberFormatException ex) {
            logger.error(prepare("Ошибка в методе инициализации параметров сервера: \n" + ex.getMessage()));
        }

        if (properties.getProperty("ip").equals("")) {
            logger.debug(prepare("IP сервера не указан. Установим IP по умолчанию 127.0.0.1"));
            serverIp = "127.0.0.1";
        } else {
            serverIp = properties.getProperty("ip");
        }
    }

    public static Map<Integer, User> getUsersChatMap() {
        return usersChatMap;
    }

    public String prepare(String msg) {
        return String.format("[%s] [%s] %s",
                threadName,
                className,
                msg);
    }

    @Override
    public String toString() {
        return "ServerParameters: " +
                "IP= " + serverIp + " " +
                "Port= " + serverPort + " " +
                "MaxClientCounInTime= " + maxClientCount;
    }


}
