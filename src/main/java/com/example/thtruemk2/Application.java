package com.example.thtruemk2;

import com.example.thtruemk2.model.threquest;
import com.example.thtruemk2.service.MyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Random;
import java.util.Scanner;

@SpringBootApplication
@Slf4j
public class Application implements CommandLineRunner {

    @Autowired
    private MyService myService;  // Inject MyService từ Spring Context
    @Autowired
    private Bot bot;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);  // Khởi chạy Spring Boot
    }

    @Override
    public void run(String... args) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            botsApi.registerBot(bot); // Đăng ký bot
            bot.getproxy();
        } catch (TelegramApiException e) {
            log.error("Lỗi khi đăng ký bot: " + e.getMessage());
        }
    }


}
