package com.mindary.aichat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AIChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIChatApplication.class, args);
    }
}
