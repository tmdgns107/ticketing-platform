package com.ticketing.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Kept separate from the main application class so that web-slice tests
 * ({@code @WebMvcTest}) don't drag in JPA auditing and fail on an empty metamodel.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {}
