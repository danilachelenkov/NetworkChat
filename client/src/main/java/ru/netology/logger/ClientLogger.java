package ru.netology.logger;

import ru.netology.FileLogger;
import ru.netology.LevelLog;

public class ClientLogger implements Writable {
    private static ClientLogger INSTANCE;
    private final FileLogger fileLogger;

    public ClientLogger() throws Exception {
        fileLogger = new FileLogger();
    }

    public static ClientLogger getInstance() throws Exception {
        if (INSTANCE == null) {
            synchronized (ClientLogger.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ClientLogger();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void info(String msg) {
        synchronized (fileLogger) {
            fileLogger.log(LevelLog.INFO, msg);
        }
    }

    @Override
    public void debug(String msg) {
        synchronized (fileLogger) {
            fileLogger.log(LevelLog.DEBUG, msg);
        }
    }

    @Override
    public void warn(String msg) {
        synchronized (fileLogger) {
            fileLogger.log(LevelLog.WARN, msg);
        }
    }

    @Override
    public void error(String msg) {
        synchronized (fileLogger) {
            fileLogger.log(LevelLog.ERROR, msg);
        }
    }

    @Override
    public void serv(String msg) {
        synchronized (fileLogger) {
            fileLogger.log(LevelLog.SERVER, msg);
        }
    }
}
