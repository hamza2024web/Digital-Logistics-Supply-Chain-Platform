// Fichier : src/main/java/com/spring/digital_logistics/IntegrationTestBase.java
package com.spring.digital_logistics;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public abstract class IntegrationTestBase {
    static PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:15-alpine")
            .withNetworkAliases("postgres-for-test")
            .withNetworkMode("jenkins-net");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        database.start();

        String jdbcUrl = String.format("jdbc:postgresql://postgres-for-test:%d/%s",
                5432,
                database.getDatabaseName());

        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("jwt.secret", () -> "secret-key-for-integration-tests");
        registry.add("spring.docker.compose.enabled", () -> "false");
    }
}