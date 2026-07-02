package com.slotbooking;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for all integration tests requiring a live database.
 * Leverages Testcontainers to bootstrap a real PostgreSQL instance.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("jwt.secret", () -> "4b2c1f93e6d8a7c5b9e0f1a2d3c4b5a69876543210fedcba0987654321fedcba");
        registry.add("jwt.expiration", () -> "3600000");
        registry.add("RAZORPAY_KEY_ID", () -> "rzp_test_mockkey");
        registry.add("RAZORPAY_KEY_SECRET", () -> "mocksecret");
        registry.add("RAZORPAY_WEBHOOK_SECRET", () -> "mockwebhooksecret");
    }
}
