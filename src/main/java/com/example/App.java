package com.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class App {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080), 0
        );

        server.createContext("/", (HttpExchange exchange) -> {

            String response = "Welcome to My Simple Java Application!";

            exchange.sendResponseHeaders(200, response.length());

            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response.getBytes());
            }
        });

        server.start();

        System.out.println("Application started on port 8080");
    }
}
