package ru.netology.application;

import ru.netology.logger.ClientLogger;

public class ApplicationClient {
    public static void main(String[] args) {
        try {
            Client client = new Client(new ClientLogger());
            client.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
