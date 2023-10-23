package ru.netology.logger;

import ru.netology.FileLogger;
import ru.netology.LevelLog;

public class ServerLogger implements Writable {
    private static ServerLogger INSTANCE;
    private final FileLogger fileLogger;

    private ServerLogger() throws Exception {
        fileLogger = new FileLogger();
    }

    public static ServerLogger getInstance() throws Exception {
        if (INSTANCE == null) {
            synchronized (ServerLogger.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ServerLogger();
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
