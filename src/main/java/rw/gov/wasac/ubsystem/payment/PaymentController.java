package rw.gov.wasac.ubsystem.payment;

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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE')")
    @Operation(summary = "Record a payment against a bill")
    public ResponseEntity<Payment> recordPayment(@Valid @RequestBody PaymentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.recordPayment(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE')")
    @Operation(summary = "Get all payments")
    public ResponseEntity<List<Payment>> getAll() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<Payment> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/bill/{billId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get payments by bill")
    public ResponseEntity<List<Payment>> getByBill(@PathVariable UUID billId) {
        return ResponseEntity.ok(paymentService.getPaymentsByBill(billId));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get payment history by customer")
    public ResponseEntity<List<Payment>> getByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(paymentService.getPaymentsByCustomer(customerId));
    }
}