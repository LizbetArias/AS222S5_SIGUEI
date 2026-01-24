package pe.edu.vallegrande.report_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.report_service.dto.ReportDto;
import pe.edu.vallegrande.report_service.service.ReportService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService service;

    // 🔍 Listar todos
    @GetMapping
    public Flux<ReportDto> findAll() {
        return service.findAll();
    }

    // 🔍 Buscar por ID
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ReportDto>> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    // ✅ Insertar nuevo reporte
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ReportDto> create(@Valid @RequestBody ReportDto dto) {
        return service.save(dto);
    }

    // 🛠️ Actualizar
    @PutMapping("/{id}")
    public Mono<ReportDto> update(@PathVariable Integer id, @Valid @RequestBody ReportDto dto) {
        return service.update(id, dto);
    }

    // ❌ Eliminación lógica (status = I)
    @PutMapping("/disable/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> disable(@PathVariable Integer id) {
        return service.disable(id);
    }

    // ♻️ Restaurar lógica (status = A)
    @PutMapping("/restore/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> restore(@PathVariable Integer id) {
        return service.restore(id);
    }

    // 🗑️ Eliminación física
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Integer id) {
        return service.delete(id);
    }

    // ❓ Validar si existe un reporte por año y trimestre (usado antes de insertar)
    @GetMapping("/exist")
    public Mono<ResponseEntity<Boolean>> existsByYearAndTrimester(
            @RequestParam Integer year,
            @RequestParam String trimester) {
        return service.existsByYearAndTrimester(year, trimester)
                .map(ResponseEntity::ok);
    }
}
