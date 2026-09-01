package com.ticketing.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Full-context integration test. Requires a Docker daemon (Testcontainers spins up
 * MySQL via the {@code jdbc:tc:} URL in {@code application-test.yml}).
 *
 * <p>Excluded from the default {@code test} task; run with {@code ./gradlew integrationTest}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag("integration")
@ActiveProfiles("test")
@SpringBootTest
public @interface IntegrationTest {}
