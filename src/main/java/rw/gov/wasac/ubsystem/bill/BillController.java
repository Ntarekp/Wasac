package rw.gov.wasac.ubsystem.bill;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.gov.wasac.ubsystem.security.SecurityService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Tag(name = "Bill Management")
public class BillController {

    private final BillService billService;
    private final SecurityService securityService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
    @Operation(summary = "Generate a bill from a meter reading")
    public ResponseEntity<Bill> generate(@Valid @RequestBody BillGenerationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billService.generateBill(dto));
    }

    @PostMapping("/generate-batch")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
    @Operation(summary = "Generate bills for all readings in a billing month/year")
    public ResponseEntity<BillBatchResultDTO> generateBatch(@Valid @RequestBody BillBatchGenerationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billService.generateMonthlyBills(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE')")
    @Operation(summary = "Get all bills")
    public ResponseEntity<List<Bill>> getAll() {
        return ResponseEntity.ok(billService.getAllBills());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @Operation(summary = "Get bills for the logged-in customer")
    public ResponseEntity<List<Bill>> getMyBills() {
        UUID customerId = securityService.requireLinkedCustomerId();
        return ResponseEntity.ok(billService.getBillsByCustomer(customerId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get bill by ID")
    public ResponseEntity<Bill> getById(@PathVariable UUID id) {
        Bill bill = billService.getBillById(id);
        securityService.verifyBillAccess(bill);
        return ResponseEntity.ok(bill);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get bills by customer")
    public ResponseEntity<List<Bill>> getByCustomer(@PathVariable UUID customerId) {
        securityService.verifyCustomerAccess(customerId);
        return ResponseEntity.ok(billService.getBillsByCustomer(customerId));
    }

    @GetMapping("/reference/{ref}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get bill by reference")
    public ResponseEntity<Bill> getByReference(@PathVariable String ref) {
        Bill bill = billService.getBillByReference(ref);
        securityService.verifyBillAccess(bill);
        return ResponseEntity.ok(bill);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE')")
    @Operation(summary = "Approve a bill")
    public ResponseEntity<Bill> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(billService.approveBill(id));
    }
}
