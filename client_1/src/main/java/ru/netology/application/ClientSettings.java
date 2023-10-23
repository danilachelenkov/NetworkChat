package ru.netology.application;

import ru.netology.logger.Writable;

import java.io.IOException;
import java.util.Properties;

public class ClientSettings {
    private String serverIpAdress;
    private Integer serverPort;
    private boolean isLoadError = false;
    private final Writable logger;
    private final String propFileName;
    private final Properties properties = new Properties();

    public ClientSettings(String propertiesFileName, Writable clientLogger) {
        this.propFileName = propertiesFileName;
        this.logger = clientLogger;

        initialize();
    }

    private void initialize() {
        try {
            logger.debug(Client.prepare("Выполняется загрузка параметров настройки клиента для его инициализации"));
            properties.load(this.getClass().getClassLoader().getResourceAsStream(propFileName));

            logger.debug(Client.prepare("ФАйл с настройками загружен. Выполнется чтение настроек"));

            if (properties.getProperty("serverIp").equals("")) {
                logger.error("IP сервера не определен в файле конфигураций клиента. Запуск клиента невозможен");
                isLoadError = true;
            } else {
                serverIpAdress = properties.getProperty("serverIp");
            }

            if (properties.getProperty("serverPort").equals("")) {
                logger.error("Порт сервера не определен в файле конфигураций клиента. Запуск клиента невозможен");
                isLoadError = true;
            } else {
                serverPort = Integer.parseInt(properties.getProperty("serverPort"));
            }

        } catch (IOException | NumberFormatException | NullPointerException ex) {
            logger.error(Client.prepare("Ошибка в процессе инициализации параметров клиента:\n" + ex.getMessage()));
        }
    }

    @Override
    public String toString() {
        return "Client settings:" +
                " Server IP = " +
                serverIpAdress +
                " Server Port = " +
                serverPort;
    }

    public String getServerIpAdress() {
        return serverIpAdress;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public boolean isLoadError() {
        return isLoadError;
    }

}
