package rw.gov.wasac.ubsystem.bill;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BillBatchGenerationDTO {

    @NotNull(message = "Billing month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer billingMonth;

    @NotNull(message = "Billing year is required")
    @Min(value = 2000, message = "Year must be 2000 or later")
    private Integer billingYear;
}
