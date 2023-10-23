package ru.netology.application;

import ru.netology.logger.ServerLogger;

public class ApplicationServer {
    public static void main(String[] args) throws Exception {
        Server server = new Server(ServerLogger.getInstance());
        server.start();
    }
}
