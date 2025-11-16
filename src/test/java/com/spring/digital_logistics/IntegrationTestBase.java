// Fichier : src/main/java/com/spring/digital_logistics/IntegrationTestBase.java
package com.spring.digital_logistics;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@SpringBootTest
@Testcontainers
public abstract class IntegrationTestBase {

    static PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:15-alpine")
            .withStartupTimeout(Duration.ofMinutes(3));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        database.start();

        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");

        registry.add("jwt.secret", () -> "une-fausse-cle-pour-les-tests-qui-fonctionne");
        registry.add("spring.docker.compose.enabled", () -> "false");
    }
}