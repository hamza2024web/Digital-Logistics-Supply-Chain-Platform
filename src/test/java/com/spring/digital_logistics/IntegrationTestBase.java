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
            .withNetworkMode("jenkins-net")
            .withNetworkAliases("db-test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        database.start();

        final String jdbcUrl = String.format("jdbc:postgresql://db-test:5432/%s", database.getDatabaseName());

        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("jwt.secret", () -> "un-secret-solide-pour-les-tests");
    }
}