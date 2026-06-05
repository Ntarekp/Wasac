package rw.gov.wasac.ubsystem.penalty;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.gov.wasac.ubsystem.bill.Bill;

import java.util.List;

@RestController
@RequestMapping("/api/penalties")
@RequiredArgsConstructor
@Tag(name = "Penalty Management")
public class PenaltyController {

    private final PenaltyService penaltyService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Manually trigger late payment penalty application")
    public ResponseEntity<String> applyNow() {
        penaltyService.applyLatePenalties();
        return ResponseEntity.ok("Late payment penalties applied successfully");
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FINANCE')")
    @Operation(summary = "Get all overdue bills")
    public ResponseEntity<List<Bill>> getOverdue() {
        return ResponseEntity.ok(penaltyService.getOverdueBills());
    }
}