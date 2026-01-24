package pe.edu.vallegrande.report_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pe.edu.vallegrande.report_service.config.DotenvInitializer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ContextConfiguration(initializers = DotenvInitializer.class)
public class ReportCoreServiceApplicationTests {

	@Value("${JWT_JWK_SET_URI}")
	private String jwtJwkSetUri;

	@Test
	void contextLoads() {
		// Esta prueba solo verifica si el contexto de Spring se carga correctamente
		// El archivo .env debería haber cargado las variables de entorno
	}

	@Test
	void testJwtJwkSetUri() {
		// Verifica que la variable JWT_JWK_SET_URI esté correctamente cargada
		System.out.println("JWT JWK Set URI: " + jwtJwkSetUri); // Muestra el valor para depuración
		assertNotNull(jwtJwkSetUri); // Verifica que no sea null
	}
}
