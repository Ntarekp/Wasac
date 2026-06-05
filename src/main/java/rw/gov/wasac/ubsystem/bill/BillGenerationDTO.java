package rw.gov.wasac.ubsystem.bill;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class BillGenerationDTO {

    @NotNull(message = "Reading ID is required")
    private UUID readingId;
}