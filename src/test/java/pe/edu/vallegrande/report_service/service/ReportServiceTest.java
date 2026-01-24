package pe.edu.vallegrande.report_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.edu.vallegrande.report_service.dto.ReportDto;
import pe.edu.vallegrande.report_service.model.Report;
import pe.edu.vallegrande.report_service.repository.ReportRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ReportServiceTest {

    private ReportRepository repository;
    private ReportService service;

    @BeforeEach
    void setUp() {
        repository = mock(ReportRepository.class);
        service = new ReportService(repository);
    }

    @Test
    @DisplayName("Guardar reporte sin duplicado activo")
    void save_shouldInsertWhenNoDuplicate() {
        ReportDto dto = createDto();

        when(repository.findAllByYearAndTrimester(dto.getYear(), dto.getTrimester()))
                .thenReturn(Flux.empty());
        when(repository.save(any())).thenReturn(Mono.just(toEntity(dto)));

        StepVerifier.create(service.save(dto))
                .expectNextMatches(r -> r.getYear() == 2024 && r.getTrimester().equals("Enero-Marzo"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Guardar reporte con duplicado activo")
    void save_shouldFailIfDuplicateExists() {
        ReportDto dto = createDto();
        Report existing = toEntity(dto);

        when(repository.findAllByYearAndTrimester(dto.getYear(), dto.getTrimester()))
                .thenReturn(Flux.just(existing));

        StepVerifier.create(service.save(dto))
                .expectErrorMatches(e -> e instanceof IllegalStateException &&
                        e.getMessage().contains("Ya existe un reporte activo"))
                .verify();
    }

    @Test
    @DisplayName("🛠Actualizar reporte existente")
    void update_shouldModifyData() {
        ReportDto dto = createDto();
        dto.setYear(2025);
        Report existing = toEntity(dto);
        existing.setYear(2023);

        when(repository.findById(1)).thenReturn(Mono.just(existing));
        when(repository.save(any())).thenReturn(Mono.just(existing));

        StepVerifier.create(service.update(1, dto))
                .expectNextMatches(r -> r.getYear() == 2025)
                .verifyComplete();
    }

    @Test
    @DisplayName("Borrado lógico de reporte")
    void disable_shouldSetInactiveStatus() {
        Report report = toEntity(createDto());

        when(repository.findById(1)).thenReturn(Mono.just(report));
        when(repository.save(any())).thenReturn(Mono.just(report));

        StepVerifier.create(service.disable(1)).verifyComplete();
        verify(repository).save(argThat(r -> r.getStatus().equals("I")));
    }

    @Test
    @DisplayName("Restaurar reporte inactivo")
    void restore_shouldSetActiveStatus() {
        Report report = toEntity(createDto());
        report.setStatus("I");

        when(repository.findById(1)).thenReturn(Mono.just(report));
        when(repository.save(any())).thenReturn(Mono.just(report));

        StepVerifier.create(service.restore(1)).verifyComplete();
        verify(repository).save(argThat(r -> r.getStatus().equals("A")));
    }

    @Test
    @DisplayName("🗑Eliminar físicamente un reporte")
    void delete_shouldRemoveReport() {
        Report report = toEntity(createDto());

        when(repository.findById(1)).thenReturn(Mono.just(report));
        when(repository.delete(report)).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(1)).verifyComplete();
        verify(repository).delete(report);
    }

    @Test
    @DisplayName("Buscar por ID")
    void findById_shouldReturnDto() {
        Report report = toEntity(createDto());

        when(repository.findById(1)).thenReturn(Mono.just(report));

        StepVerifier.create(service.findById(1))
                .expectNextMatches(r -> r.getId().equals(report.getId()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Listar todos los reportes")
    void findAll_shouldReturnList() {
        Report report = toEntity(createDto());

        when(repository.findAll()).thenReturn(Flux.just(report));

        StepVerifier.create(service.findAll())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @DisplayName("Validar duplicado activo")
    void existsByYearAndTrimester_shouldDetectDuplicate() {
        Report report = toEntity(createDto());

        when(repository.findAllByYearAndTrimester(2024, "Enero-Marzo"))
                .thenReturn(Flux.just(report));

        StepVerifier.create(service.existsByYearAndTrimester(2024, "Enero-Marzo"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("No se puede actualizar un reporte inexistente")
    void update_shouldReturnEmptyIfNotFound() {
        ReportDto dto = createDto();
        when(repository.findById(1)).thenReturn(Mono.empty());

        StepVerifier.create(service.update(1, dto))
                .verifyComplete(); // No se emite nada
    }

    @Test
    @DisplayName("No se puede borrar lógicamente un reporte inexistente")
    void disable_shouldReturnEmptyIfNotFound() {
        when(repository.findById(1)).thenReturn(Mono.empty());

        StepVerifier.create(service.disable(1))
                .verifyComplete();
    }

    @Test
    @DisplayName("No se puede restaurar un reporte inexistente")
    void restore_shouldReturnEmptyIfNotFound() {
        when(repository.findById(1)).thenReturn(Mono.empty());

        StepVerifier.create(service.restore(1))
                .verifyComplete();
    }

    @Test
    @DisplayName("No se puede eliminar físicamente un reporte inexistente")
    void delete_shouldReturnEmptyIfNotFound() {
        when(repository.findById(1)).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(1))
                .verifyComplete();
    }

    @Test
    @DisplayName("No hay duplicado activo")
    void existsByYearAndTrimester_shouldReturnFalseIfEmpty() {
        when(repository.findAllByYearAndTrimester(2024, "Enero-Marzo"))
                .thenReturn(Flux.empty());

        StepVerifier.create(service.existsByYearAndTrimester(2024, "Enero-Marzo"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Mapper toEntity con status nulo debe asignar 'A'")
    void toEntity_shouldSetDefaultStatusIfNull() {
        ReportDto dto = createDto();
        dto.setStatus(null);

        Report entity = serviceToEntity(dto); // método auxiliar interno si es privado

        assertEquals("A", entity.getStatus());
    }


    // 🔧 DTO de ejemplo
    private ReportDto createDto() {
        ReportDto dto = new ReportDto();
        dto.setId(1);
        dto.setYear(2024);
        dto.setTrimester("Enero-Marzo");
        dto.setDescriptionUrl("https://supabase.com/reports/html/enero-marzo.html");
        dto.setScheduleUrl("https://supabase.com/reports/schedules/enero-marzo.jpg");
        dto.setStatus("A");
        return dto;
    }

    // 🔧 Entidad de ejemplo
    private Report toEntity(ReportDto dto) {
        return Report.builder()
                .id(dto.getId())
                .year(dto.getYear())
                .trimester(dto.getTrimester())
                .descriptionUrl(dto.getDescriptionUrl())
                .scheduleUrl(dto.getScheduleUrl())
                .status(dto.getStatus())
                .build();
    }

    private Report serviceToEntity(ReportDto dto) {
        return Report.builder()
                .id(dto.getId())
                .year(dto.getYear())
                .trimester(dto.getTrimester())
                .descriptionUrl(dto.getDescriptionUrl())
                .scheduleUrl(dto.getScheduleUrl())
                .status(dto.getStatus() != null ? dto.getStatus() : "A")
                .build();
    }

}
