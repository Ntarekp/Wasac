package rw.gov.wasac.ubsystem.message;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE')")
    @Operation(summary = "Get all notification messages")
    public ResponseEntity<List<Message>> getAll() {
        return ResponseEntity.ok(messageService.getAllMessages());
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get notifications for a customer")
    public ResponseEntity<List<Message>> getByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(messageService.getMessagesByCustomer(customerId));
    }
}