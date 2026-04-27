package br.com.ecommerce.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AbstractIntegrationTest {

    @LocalServerPort
    private int port;

//    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:14")
                    .withDatabaseName("testdb")
                    .withUsername("admin")
                    .withPassword("admin");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configurarProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.secret", () -> "Valor-secret");
    }

    @BeforeEach
    void setupRestAssured() {
        RestAssured.baseURI = "http://localhost:" + port + "/";
    }
}
