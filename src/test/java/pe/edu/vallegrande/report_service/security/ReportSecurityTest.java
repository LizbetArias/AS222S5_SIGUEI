package pe.edu.vallegrande.report_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import pe.edu.vallegrande.report_service.config.DotenvInitializer;
import pe.edu.vallegrande.report_service.util.FirebaseTokenUtil;

import java.time.Duration;

@SpringBootTest
@AutoConfigureWebTestClient
@ContextConfiguration(initializers = DotenvInitializer.class)
public class ReportSecurityTest {

    @Autowired
    private ApplicationContext context;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .responseTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Test
    void shouldReturn401_whenNoTokenProvided() {
        webTestClient.get()
                .uri("/api/reports")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturn200_whenUserHasCorrectRole() {
        // ✅ Este token puede ser USER o ADMIN
        String token = "Bearer " + FirebaseTokenUtil.getTokenFromFirebase();

        webTestClient.get()
                .uri("/api/reports")
                .header("Authorization", token)
                .exchange()
                .expectStatus().isOk();
    }
}
