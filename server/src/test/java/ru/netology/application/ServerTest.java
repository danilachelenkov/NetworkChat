package ru.netology.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.logger.ServerLogger;

public class ServerTest {
    private Server server;

    @BeforeEach
    public void beforeEachTest() {
        System.out.println("Инициализация экземпляра перед выполнением теста");
        try {
            server = new Server(ServerLogger.getInstance());
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }

    @AfterEach
    public void afterEachTests() {
        System.out.println("Устанавливаем пустую ссылку для эксземпляров после выполнения каждого теста.\r\n");
        server = null;
    }

    @Test
    public void test_EqualsContentOfPrepare() {
        String result = server.prepare("Тестовое сообщение");
        String expected = "[main] [ru.netology.application.Server] Тестовое сообщение";
        Assertions.assertEquals(expected,result);
    }
}
