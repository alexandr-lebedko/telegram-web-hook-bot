package net.lebedko.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;

import javax.annotation.PostConstruct;
import java.io.Serializable;

@SpringBootApplication
@RestController
public class TelegramApplication {
    static {
        ApiContextInitializer.init();
    }

    public static void main(String[] args) {
        SpringApplication.run(TelegramApplication.class, args);
    }

    @Autowired
    private TelegramBot telegramBot;

    @Value("${telegram.bot.keyStore}")
    private String keyStore;
    @Value("${telegram.bot.keyStorePass}")
    private String keyStorePass;
    @Value("${telegram.bot.externalUrl}")
    private String externalUrl;
    @Value("${telegram.bot.internalUrl}")
    private String internalUrl;
    @Value("${telegram.bot.pathToCertificate}")
    private String pathToCert;

    @PostConstruct
    public void registerBot() {
        try {
            new TelegramBotsApi(keyStore, keyStorePass, externalUrl, internalUrl, pathToCert)
                    .registerBot(telegramBot);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }


    @GetMapping
    public String hello(@RequestParam String chatId) throws TelegramApiException {
        telegramBot.executeAsync(new SendMessage(chatId, "Hello World"), new SentCallback<>() {
            @Override
            public void onResult(BotApiMethod<Message> method, Message response) {
                System.out.println("On Result Hook");
            }

            @Override
            public void onError(BotApiMethod<Message> method, TelegramApiRequestException apiException) {
                System.out.println("On Error Hook");
            }

            @Override
            public void onException(BotApiMethod<Message> method, Exception exception) {
                System.out.println("On Exception Hook");
            }
        });

        return "Hello World";
    }

    @Component
    public static class TelegramBot extends TelegramWebhookBot {

        @Override
        public BotApiMethod<? extends Serializable> onWebhookUpdateReceived(Update update) {
            System.out.println("Chat Id: " + update.getMessage().getChatId());
            return new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("Hello Friend. I'm in cloud");
        }

        @Override
        public String getBotUsername() {
            return "@oleksandr_lebedko_bot";
        }

        @Override
        public String getBotToken() {
            return null;
        }

        @Override
        public String getBotPath() {
            return "oleksandr_lebedko_bot";
        }
    }
}
