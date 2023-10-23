package ru.netology.logger;

public interface Writable {
    void info(String msg);
    void debug(String msg);
    void warn(String msg);

    void error(String msg);
    void serv(String msg);
}
