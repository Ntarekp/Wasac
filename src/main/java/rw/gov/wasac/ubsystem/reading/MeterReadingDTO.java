package rw.gov.wasac.ubsystem.reading;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class MeterReadingDTO {

    @NotNull(message = "Meter ID is required")
    private UUID meterId;

    @NotNull(message = "Previous reading is required")
    @PositiveOrZero(message = "Previous reading cannot be negative")
    private Double previousReading;

    @NotNull(message = "Current reading is required")
    @Positive(message = "Reading must be positive")
    private Double currentReading;

    @NotNull(message = "Reading date is required")
    private LocalDate readingDate;
}