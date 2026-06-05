package rw.gov.wasac.ubsystem.meter;

import jakarta.validation.constraints.*;
import lombok.Data;
import rw.gov.wasac.ubsystem.enums.EMeterType;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class MeterDTO {

    @NotBlank(message = "Meter number is required")
    private String meterNumber;

    @NotNull(message = "Meter type is required")
    private EMeterType meterType;

    @NotNull(message = "Installation date is required")
    private LocalDate installationDate;

    @NotNull(message = "Customer ID is required")
    private UUID customerId;
}