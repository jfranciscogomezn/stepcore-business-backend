package com.stepcore.business;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class StepcoreBusinessApplication {

    public static void main(final String[] args) {
        SpringApplication.run(StepcoreBusinessApplication.class, args);
    }
}
