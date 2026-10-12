package com.bunkbuster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BunkBusterApplication {
    public static void main(String[] args) {
        SpringApplication.run(BunkBusterApplication.class, args);
        System.out.println("🚀 BunkBuster Started → http://localhost:8080");
    }
}