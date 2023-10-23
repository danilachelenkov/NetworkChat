package ru.netology.application;

import ru.netology.Message;
import ru.netology.logger.Writable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;

public class ClientReceiveHandler implements Runnable {
    private final String className = getClass().getName();
    private final String threadName = Thread.currentThread().getName();
    private final Socket socket;
    private final Printable printer;
    private final Writable logger;

    public ClientReceiveHandler(Socket socket, Printable printer, Writable logger) {
        this.socket = socket;
        this.printer = printer;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                printer.print(doPrinterFormat((Message)
                                new ObjectInputStream(
                                        socket.getInputStream())
                                        .readObject()
                        )
                );
            }
        } catch (IOException | ClassNotFoundException ex) {
            logger.error(prepare("Ошибка при получении сообщений:\n" + ex.getMessage()));
        }
    }

    private String prepare(String msg) {
        return String.format("[%s] [%s] %s",
                threadName,
                className,
                msg);
    }

    private String doPrinterFormat(Message message) {
        logger.debug(prepare(String.format("Получено сообщение от Сервера: {%s}", message.toString())));

        return new StringBuilder()
                .append("[")
                .append(message.getLogin())
                .append("]")
                .append("[")
                .append(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(message.getDatetime()))
                .append("] ")
                .append(message.getTextMessage())
                .toString();
    }


}
