package rw.gov.wasac.ubsystem.payment;

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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management")
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityService securityService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE')")
    @Operation(summary = "Record a pending payment against an approved bill")
    public ResponseEntity<Payment> recordPayment(@Valid @RequestBody PaymentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.recordPayment(dto));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE')")
    @Operation(summary = "Approve a pending payment and update bill balance")
    public ResponseEntity<Payment> approvePayment(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.approvePayment(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE')")
    @Operation(summary = "Get all payments")
    public ResponseEntity<List<Payment>> getAll() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @Operation(summary = "Get payment history for the logged-in customer")
    public ResponseEntity<List<Payment>> getMyPayments() {
        UUID customerId = securityService.requireLinkedCustomerId();
        return ResponseEntity.ok(paymentService.getPaymentsByCustomer(customerId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<Payment> getById(@PathVariable UUID id) {
        Payment payment = paymentService.getPaymentById(id);
        securityService.verifyPaymentAccess(payment);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/bill/{billId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get payments by bill")
    public ResponseEntity<List<Payment>> getByBill(@PathVariable UUID billId) {
        List<Payment> payments = paymentService.getPaymentsByBill(billId);
        if (!payments.isEmpty()) {
            securityService.verifyPaymentAccess(payments.getFirst());
        }
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get payment history by customer")
    public ResponseEntity<List<Payment>> getByCustomer(@PathVariable UUID customerId) {
        securityService.verifyCustomerAccess(customerId);
        return ResponseEntity.ok(paymentService.getPaymentsByCustomer(customerId));
    }
}
