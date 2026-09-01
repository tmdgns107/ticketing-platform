package com.ticketing.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Full-context integration test. Requires a Docker daemon — Testcontainers starts MySQL and
 * Redis (see {@link TestcontainersConfiguration}).
 *
 * <p>Excluded from the default {@code test} task; run with {@code ./gradlew integrationTest}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag("integration")
@ActiveProfiles("test")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
public @interface IntegrationTest {}
