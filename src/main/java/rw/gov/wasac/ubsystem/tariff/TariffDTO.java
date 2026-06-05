package rw.gov.wasac.ubsystem.tariff;

import jakarta.validation.constraints.*;
import lombok.Data;
import rw.gov.wasac.ubsystem.enums.EMeterType;
import rw.gov.wasac.ubsystem.enums.ETariffType;

import java.time.LocalDate;
import java.util.List;

@Data
public class TariffDTO {

    @NotBlank(message = "Tariff name is required")
    private String name;

    @NotNull(message = "Meter type is required")
    private EMeterType meterType;

    @NotNull(message = "Tariff type is required")
    private ETariffType tariffType;

    // Required for FLAT_RATE
    @Positive(message = "Unit price must be positive")
    private Double unitPrice;

    @NotNull(message = "Service charge is required")
    @PositiveOrZero
    private Double serviceCharge;

    @NotNull(message = "VAT percentage is required")
    @PositiveOrZero
    private Double vatPercentage;

    @NotNull(message = "Late payment penalty percentage is required")
    @PositiveOrZero
    private Double latePaymentPenaltyPercentage;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    // Required for TIER_BASED
    private List<TierDTO> tiers;

    @Data
    public static class TierDTO {
        @NotNull @PositiveOrZero private Double fromUnit;
        @NotNull @Positive private Double toUnit;
        @NotNull @Positive private Double pricePerUnit;
    }
}