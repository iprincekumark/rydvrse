package com.rydvrse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * RYDVRSE Platform — Main Entry Point
 *
 * This is the single deployable application that assembles all domain modules.
 * Each module owns its own domain models, services, and APIs.
 * Inter-module communication happens via:
 *   1. Spring ApplicationEvents (synchronous, in-process)
 *   2. Kafka (asynchronous, durable for analytics & notifications)
 *   3. Direct service interface calls (for same-transaction operations)
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class RydvrseApplication {

    public static void main(String[] args) {
        SpringApplication.run(RydvrseApplication.class, args);
    }
}
