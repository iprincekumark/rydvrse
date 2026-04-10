package com.rydvrse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RydvrseApplication {

    public static void main(String[] args) {
        SpringApplication.run(RydvrseApplication.class, args);
    }
}
