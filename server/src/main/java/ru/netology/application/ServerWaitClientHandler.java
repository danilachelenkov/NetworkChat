package ru.netology.application;

import ru.netology.Message;
import ru.netology.MessageType;
import ru.netology.logger.Writable;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerWaitClientHandler implements Runnable {
    private final String className = getClass().getName();
    private final String threadName = Thread.currentThread().getName();

    private Integer serverPort;
    private Integer maxClientCount;
    private ServerSocket serverSocket;
    private Socket socket;
    private Writable logger;
    private ExecutorService executorReceiver;

    public ServerWaitClientHandler(Writable logger, Integer serverPort, Integer maxClientCount) {
        this.serverPort = serverPort;
        this.maxClientCount = maxClientCount;
        this.logger = logger;

        executorReceiver = Executors.newFixedThreadPool(maxClientCount);
    }


    @Override
    public void run() {
        waitClientConnection();
    }

    private void waitClientConnection() {
        try {
            logger.debug(prepare("Запущен поток ожидания новых подключений"));

            logger.debug(prepare("Выполняется инициализация SocketServer"));
            serverSocket = new ServerSocket(serverPort, maxClientCount);

            logger.debug(prepare("Инициализация SocketServer прошла успешно. Максимальное количество возможных подключений к серверу: "
                    + maxClientCount));

            logger.debug(prepare("Запускаем беспонечный цикл ожидания подключений. Выполняем его пока SocketServer не закрыт."));

            while (!serverSocket.isClosed()) {

                logger.debug(prepare("Поток находится в ожидании подключения нового клиента..."));
                socket = serverSocket.accept();

                logger.debug(prepare(String.format("Обнаружено новое подключение с параметрами SocketPort: %s", socket.getPort())));

                sendInstructionClient(socket);
                logger.debug(prepare("Отправлено сообщение с инструкциями по регистрации пользователю"));

                executorReceiver.execute(new ServerReceiverMessageHandler(socket, logger));
            }
        } catch (IOException ex) {
            logger.error(prepare("Ошибка в процессе работы ServerSocket:\n" + ex.getMessage()));
        }
    }

    private void sendInstructionClient(Socket socket) {
        try {
            new ObjectOutputStream(socket.getOutputStream())
                    .writeObject(
                            new Message(
                                    MessageType.TO_USER,
                                    Server.SERVER_LOGIN,
                                    "Выподключились к сетевому чату. " +
                                            "Для того, чтобы начать общаться необходимо пройти регистрацию на сервере: " +
                                            "введите свой <Login>, а после команду /reg. Например: <Login>/reg",
                                    socket.getLocalPort()));
        } catch (IOException ex) {
            logger.error(prepare("Ошибка отправки сообщения в методе sendInstructionMessage:\n" + ex.getMessage()));
        }
    }

    private String prepare(String msg) {
        return String.format("[%s] [%s] %s",
                threadName,
                className,
                msg);
    }
}
