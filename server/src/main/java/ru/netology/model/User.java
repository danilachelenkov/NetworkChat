package ru.netology.model;

import java.net.Socket;
import java.util.Date;

public class User {
    private final String login;
    private final Date dateLogining;
    private final Socket socket;

    public User(String login, Socket socket) {
        this.login = login;
        this.dateLogining = new Date(System.currentTimeMillis());
        ;
        this.socket = socket;
    }

    public String getLogin() {
        return login;
    }

    public Date getDateLogining() {
        return dateLogining;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public String toString() {
        return " Login=" + login +
                " SocketPort=" + socket.getPort() +
                " DateTimeRegistration=" + dateLogining.toString();
    }
}
