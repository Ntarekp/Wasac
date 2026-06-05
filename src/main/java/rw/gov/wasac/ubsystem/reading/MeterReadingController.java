package rw.gov.wasac.ubsystem.reading;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/readings")
@RequiredArgsConstructor
@Tag(name = "Meter Reading Management")
public class MeterReadingController {

    private final MeterReadingService readingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
    @Operation(summary = "Capture a meter reading")
    public ResponseEntity<MeterReading> capture(@Valid @RequestBody MeterReadingDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(readingService.captureReading(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR', 'ROLE_FINANCE')")
    @Operation(summary = "Get all meter readings")
    public ResponseEntity<List<MeterReading>> getAll() {
        return ResponseEntity.ok(readingService.getAllReadings());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR', 'ROLE_FINANCE', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get reading by ID")
    public ResponseEntity<MeterReading> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(readingService.getReadingById(id));
    }

    @GetMapping("/meter/{meterId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get readings by meter")
    public ResponseEntity<List<MeterReading>> getByMeter(@PathVariable UUID meterId) {
        return ResponseEntity.ok(readingService.getReadingsByMeter(meterId));
    }
}