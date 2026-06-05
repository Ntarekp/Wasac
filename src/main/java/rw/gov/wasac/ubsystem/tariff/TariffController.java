package rw.gov.wasac.ubsystem.tariff;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.gov.wasac.ubsystem.enums.EMeterType;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tariffs")
@RequiredArgsConstructor
@Tag(name = "Tariff Management")
public class TariffController {

    private final TariffService tariffService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create a new tariff configuration")
    public ResponseEntity<Tariff> create(@Valid @RequestBody TariffDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tariffService.createTariff(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE')")
    @Operation(summary = "Get all tariffs")
    public ResponseEntity<List<Tariff>> getAll() {
        return ResponseEntity.ok(tariffService.getAllTariffs());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE')")
    @Operation(summary = "Get tariff by ID")
    public ResponseEntity<Tariff> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(tariffService.getTariffById(id));
    }

    @GetMapping("/active/{meterType}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE', 'ROLE_OPERATOR')")
    @Operation(summary = "Get active tariff by meter type")
    public ResponseEntity<Tariff> getActive(@PathVariable EMeterType meterType) {
        return ResponseEntity.ok(tariffService.getActiveTariff(meterType));
    }

    @GetMapping("/{id}/tiers")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE')")
    @Operation(summary = "Get tiers for a tariff")
    public ResponseEntity<List<TariffTier>> getTiers(@PathVariable UUID id) {
        return ResponseEntity.ok(tariffService.getTiersForTariff(id));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deactivate a tariff without creating a new version")
    public ResponseEntity<Tariff> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(tariffService.deactivateTariff(id));
    }
}