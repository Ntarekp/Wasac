package rw.gov.wasac.ubsystem.message;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.gov.wasac.ubsystem.security.SecurityService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class MessageController {

    private final MessageService messageService;
    private final SecurityService securityService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE')")
    @Operation(summary = "Get all notification messages")
    public ResponseEntity<List<Message>> getAll() {
        return ResponseEntity.ok(messageService.getAllMessages());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @Operation(summary = "Get notifications for the logged-in customer")
    public ResponseEntity<List<Message>> getMyMessages() {
        UUID customerId = securityService.requireLinkedCustomerId();
        return ResponseEntity.ok(messageService.getMessagesByCustomer(customerId));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get notifications for a customer")
    public ResponseEntity<List<Message>> getByCustomer(@PathVariable UUID customerId) {
        securityService.verifyCustomerAccess(customerId);
        return ResponseEntity.ok(messageService.getMessagesByCustomer(customerId));
    }
}
