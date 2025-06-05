package com.example.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example")
@EntityScan(basePackages = "com.example.core.domain")
@EnableJpaRepositories(basePackages = "com.example.api.repository")
public class BinomeMatcherApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BinomeMatcherApiApplication.class, args);
    }
}