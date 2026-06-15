package kr.ai.janus.support;

import java.util.Base64;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class IntegrationTest {

    @Container
    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("jwt.secret", () -> Base64.getEncoder().encodeToString(new byte[48]));
        registry.add("kakao.rest-api-key", () -> "dummy");
        registry.add("kakao.client-secret", () -> "dummy");
        registry.add("kakao.redirect-uri", () -> "http://localhost/callback");
        registry.add("cors.allowed-origins", () -> "http://localhost:5173");
    }
}
