package com.rabserkon.blockchain;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class P2PNodeTest {

    private void startServer(int port) throws IOException {
        // Ваш код для инициализации сервера, создания ServerSocket и принятия соединений
    }

    @Test
    public void testServer() {
        int port = 8000; // Замените на порт, который использует ваш сервер

        try {
            // Запускаем сервер
            startServer(port);

            // Создаем клиентский сокет и отправляем данные на сервер
            try (Socket clientSocket = new Socket("localhost", port)) {
                OutputStream out = clientSocket.getOutputStream();
                out.write("Hello, server!".getBytes());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Здесь можно добавить проверки на ожидаемое поведение сервера

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
