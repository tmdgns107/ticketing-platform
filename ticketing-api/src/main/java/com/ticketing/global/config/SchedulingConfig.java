package com.ticketing.global.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    /** Injected wherever "now" is needed, so tests can pin time. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
