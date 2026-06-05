package rw.gov.wasac.ubsystem.bill;

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
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Tag(name = "Bill Management")
public class BillController {

    private final BillService billService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
    @Operation(summary = "Generate a bill from a meter reading")
    public ResponseEntity<Bill> generate(@Valid @RequestBody BillGenerationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billService.generateBill(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE')")
    @Operation(summary = "Get all bills")
    public ResponseEntity<List<Bill>> getAll() {
        return ResponseEntity.ok(billService.getAllBills());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get bill by ID")
    public ResponseEntity<Bill> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(billService.getBillById(id));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get bills by customer")
    public ResponseEntity<List<Bill>> getByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(billService.getBillsByCustomer(customerId));
    }

    @GetMapping("/reference/{ref}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get bill by reference")
    public ResponseEntity<Bill> getByReference(@PathVariable String ref) {
        return ResponseEntity.ok(billService.getBillByReference(ref));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE')")
    @Operation(summary = "Approve a bill")
    public ResponseEntity<Bill> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(billService.approveBill(id));
    }
}