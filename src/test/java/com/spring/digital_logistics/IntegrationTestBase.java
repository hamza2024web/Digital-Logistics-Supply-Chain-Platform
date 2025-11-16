package com.spring.digital_logistics;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public abstract class IntegrationTestBase {

    static Network network = Network.SHARED;

    static PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:15-alpine")
            .withNetwork(network)
            .withNetworkAliases("db");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (!database.isRunning()) {
            database.start();
        }


        String jdbcUrl = String.format("jdbc:postgresql://db:%d/%s",
                database.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                database.getDatabaseName());

        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        registry.add("jwt.secret", () -> "une-fausse-cle-pour-les-tests-qui-fonctionne");
        registry.add("spring.docker.compose.enabled", () -> "false");
    }
}