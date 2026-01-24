package pe.edu.vallegrande.report_service.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.report_service.config.DotenvInitializer;
import java.util.Map;

@ContextConfiguration(initializers = DotenvInitializer.class)
public class FirebaseTokenUtil {

    public static String getTokenFromFirebase() {
        String apiKey = System.getProperty("FIREBASE_API_KEY");
        String email = System.getProperty("FIREBASE_TEST_EMAIL");
        String password = System.getProperty("FIREBASE_TEST_PASSWORD");

        if (apiKey == null || email == null || password == null) {
            throw new RuntimeException("Variables de entorno faltantes: FIREBASE_API_KEY, FIREBASE_TEST_EMAIL, FIREBASE_TEST_PASSWORD");
        }

        WebClient client = WebClient.builder().baseUrl("https://identitytoolkit.googleapis.com").build();

        JsonNode response = client.post()
                .uri("/v1/accounts:signInWithPassword?key=" + apiKey)
                .bodyValue(Map.of(
                        "email", email,
                        "password", password,
                        "returnSecureToken", true
                ))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        return response.get("idToken").asText();
    }
}
