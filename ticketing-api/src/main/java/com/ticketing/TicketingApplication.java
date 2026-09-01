package com.ticketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class TicketingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketingApplication.class, args);
    }
}
