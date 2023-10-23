package ru.netology;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class FileLogger {
    private static long lineNumber = 0;
    private static String logFileName;
    private static String logErrorFileName;
    private static String pathToLogFile;
    private static LevelLog level;
    private static String dateTimePattern;
    private FileWriter fileWriter;
    SimpleDateFormat formatter;

    public FileLogger() {

        try {
            loadLoggerConfiguration();
            this.fileWriter = new FileWriter(pathToLogFile, true);
            formatter = new SimpleDateFormat(dateTimePattern);

        } catch (Exception ex) {
            try {
                createLoggerFile(ex.getMessage(), ModeCrFile.ERROR);
            } catch (IOException ioEx) {
                throw new RuntimeException(String.format("Ошибка создания файла %s", logErrorFileName));
            }
        }
    }

    public void log(LevelLog levelLog, String msg) {
        if (levelLog.ordinal() <= level.ordinal()) {
            writeToFileLog(getLogLine(levelLog, msg));
        }
    }

    private void writeToFileLog(String line) {
        try {
            fileWriter.write(line);
            fileWriter.flush();
        } catch (IOException ex) {
            try {
                createLoggerFile(ex.getMessage(), ModeCrFile.ERROR);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при попыте создать файл createLoggerFile()" + e.getMessage());
            }
        }
    }

    private void createLoggerFile(String exMessage, ModeCrFile mode) throws IOException {
        File loggerErrorFile;

        switch (mode) {
            case ERROR ->
                    loggerErrorFile = new File(String.join("\\", System.getProperty("user.dir"), logErrorFileName));
            case WRITE ->
                    loggerErrorFile = new File(String.join("\\", System.getProperty("user.dir"), logErrorFileName));
            default -> loggerErrorFile = new File(String.join("\\", System.getProperty("user.dir"), logErrorFileName));
        }

        if (!loggerErrorFile.exists()) {
            loggerErrorFile.createNewFile();
        }

        FileWriter fw = new FileWriter(loggerErrorFile.getPath(), true);
        fw.write(exMessage);
        fw.flush();
        fw.close();
    }

    private String generateLogFileName(String logFileName) {
        return String.format("%s_%s",
                new SimpleDateFormat("ddMMyyyy").format(new Date(System.currentTimeMillis())),
                logFileName);
    }

    private String getLogLine(LevelLog levelLog, String msg) {
        lineNumber++;
        return "\n" +
                lineNumber +
                " " +
                getTimeNow() +
                " [" +
                levelLog.name() +
                "] " +
                msg;
    }

    private String getTimeNow() {
        return formatter.format(new Date(System.currentTimeMillis()));
    }

    public static String getLogFileName() {
        return logFileName;
    }

    public static String getLogErrorFileName() {
        return logErrorFileName;
    }

    public static String getPathToLogFile() {
        return pathToLogFile;
    }

    public static LevelLog getLevel() {
        return level;
    }

    private void loadLoggerConfiguration() throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("log.properties");
        Properties properties = new Properties();
        properties.load(inputStream);

        logFileName = properties.getProperty("log.file.name");

        switch (properties.getProperty("logger.level").toUpperCase()) {
            case "INFO" -> level = LevelLog.INFO;
            case "DEBUG" -> level = LevelLog.DEBUG;
            case "WARN" -> level = LevelLog.WARN;
            case "ERROR" -> level = LevelLog.ERROR;
            case "SERVER" -> level = LevelLog.SERVER;
            default -> level = LevelLog.ERROR;
        }

        if (properties.getProperty("log.file.path").equals("")) {
            pathToLogFile = String.join("\\", System.getProperty("user.dir"), generateLogFileName(logFileName));
        } else {
            pathToLogFile = String.join("\\", properties.getProperty("log.file.path"), generateLogFileName(logFileName));
            if (!new File(pathToLogFile).exists()) {
                throw new IOException("Path =" + pathToLogFile + " not found. Check the setted path in properties.file");
            }
        }

        if (properties.getProperty("log.file.error.name").equals("")) {
            logErrorFileName = generateLogFileName("error_logger.log");
        } else {
            logErrorFileName = generateLogFileName(properties.getProperty("log.file.error.name"));
        }

        if (properties.getProperty("pattern.datetime").equals("")) {
            dateTimePattern = "dd.MM.yyyy HH:mm:ss";
        } else {
            dateTimePattern = properties.getProperty("pattern.datetime");
        }

    }
}
