package pe.edu.vallegrande.report_service.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.beans.factory.annotation.Autowired;
import pe.edu.vallegrande.report_service.config.DotenvInitializer;
import pe.edu.vallegrande.report_service.config.TestSecurityConfig;
import pe.edu.vallegrande.report_service.dto.ReportDto;
import pe.edu.vallegrande.report_service.service.ReportService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ContextConfiguration(initializers = DotenvInitializer.class)
@AutoConfigureWebTestClient
@Import({ReportControllerTest.MockConfig.class, TestSecurityConfig.class})
public class ReportControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReportService reportService;

    /**
     * Configura un mock manual de ReportService usando Mockito.
     */
    @TestConfiguration
    static class MockConfig {
        @Bean
        public ReportService reportService() {
            return Mockito.mock(ReportService.class);
        }
    }

    @Test
    void shouldReturn400_whenMissingTrimester() {
        // 🧪 TEST 1: Devuelve 400 Bad Request si falta el campo "trimester"

        ReportDto dto = new ReportDto();
        dto.setYear(2025);
        dto.setDescriptionUrl("https://supabase.com/reports/html/sin-trimestre.html");
        dto.setScheduleUrl("https://supabase.com/reports/schedules/sin-trimestre.jpg");

        webTestClient.post()
                .uri("/api/reports")
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assert result.getResponseBody().containsKey("trimester");
                });
    }

    @Test
    void shouldReturn201_whenValidReportIsSaved() {
        // 🧪 TEST 2: Guarda un reporte válido correctamente (HTTP 201)

        ReportDto dto = new ReportDto();
        dto.setYear(2024);
        dto.setTrimester("Abril-Junio");
        dto.setDescriptionUrl("https://supabase.com/reports/html/abril-junio.html");
        dto.setScheduleUrl("https://supabase.com/reports/schedules/abril-junio.jpg");
        dto.setStatus("A");

        Mockito.when(reportService.save(any())).thenReturn(Mono.just(dto));

        webTestClient.post()
                .uri("/api/reports")
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.year").isEqualTo(2024)
                .jsonPath("$.trimester").isEqualTo("Abril-Junio");
    }

    @Test
    void shouldReturn404_whenNotFoundById() {
        Mockito.when(reportService.findById(99)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/reports/99")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
    }

    @Test
    void shouldReturnEmptyList_whenNoReportsExist() {
        Mockito.when(reportService.findAll()).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/reports")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ReportDto.class).hasSize(0);
    }

    @Test
    void shouldUpdateReportSuccessfully() {
        ReportDto dto = new ReportDto();
        dto.setId(1);
        dto.setYear(2024);
        dto.setTrimester("Abril-Junio");
        dto.setDescriptionUrl("https://supabase.com/reports/html/update.html"); // ✅ obligatorio
        dto.setScheduleUrl("https://supabase.com/reports/schedules/update.jpg");
        dto.setStatus("A");

        Mockito.when(reportService.update(Mockito.eq(1), any())).thenReturn(Mono.just(dto));

        webTestClient.put()
                .uri("/api/reports/1")
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.year").isEqualTo(2024)
                .jsonPath("$.trimester").isEqualTo("Abril-Junio");
    }

    @Test
    void shouldDisableReportSuccessfully() {
        Mockito.when(reportService.disable(1)).thenReturn(Mono.empty());

        webTestClient.put()
                .uri("/api/reports/disable/1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldRestoreReportSuccessfully() {
        Mockito.when(reportService.restore(1)).thenReturn(Mono.empty());

        webTestClient.put()
                .uri("/api/reports/restore/1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldDeleteReportSuccessfully() {
        Mockito.when(reportService.delete(1)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/reports/1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldReturnTrue_whenReportExistsByYearAndTrimester() {
        Mockito.when(reportService.existsByYearAndTrimester(2024, "Abril-Junio"))
                .thenReturn(Mono.just(true));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/reports/exist")
                        .queryParam("year", 2024)
                        .queryParam("trimester", "Abril-Junio")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }

}
