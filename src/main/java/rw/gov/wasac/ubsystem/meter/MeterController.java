package rw.gov.wasac.ubsystem.meter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.gov.wasac.ubsystem.enums.EStatus;
import rw.gov.wasac.ubsystem.security.SecurityService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meters")
@RequiredArgsConstructor
@Tag(name = "Meter Management")
public class MeterController {

    private final MeterService meterService;
    private final SecurityService securityService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
    @Operation(summary = "Install a new meter")
    public ResponseEntity<Meter> create(@Valid @RequestBody MeterDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(meterService.createMeter(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR', 'ROLE_FINANCE')")
    @Operation(summary = "Get all meters")
    public ResponseEntity<List<Meter>> getAll() {
        return ResponseEntity.ok(meterService.getAllMeters());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR', 'ROLE_FINANCE', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get meter by ID")
    public ResponseEntity<Meter> getById(@PathVariable UUID id) {
        Meter meter = meterService.getMeterById(id);
        securityService.verifyMeterAccess(meter);
        return ResponseEntity.ok(meter);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get meters by customer")
    public ResponseEntity<List<Meter>> getByCustomer(@PathVariable UUID customerId) {
        securityService.verifyCustomerAccess(customerId);
        return ResponseEntity.ok(meterService.getMetersByCustomer(customerId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update meter status")
    public ResponseEntity<Meter> updateStatus(@PathVariable UUID id, @RequestParam EStatus status) {
        return ResponseEntity.ok(meterService.updateStatus(id, status));
    }
}
