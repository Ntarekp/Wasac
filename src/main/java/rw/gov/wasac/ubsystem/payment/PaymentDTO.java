package rw.gov.wasac.ubsystem.payment;

import jakarta.validation.constraints.*;
import lombok.Data;
import rw.gov.wasac.ubsystem.enums.EPaymentMethod;

import java.time.LocalDate;

@Data
public class PaymentDTO {

    @NotBlank(message = "Bill reference is required")
    private String billReference;

    @NotNull(message = "Amount paid is required")
    @Positive(message = "Amount must be positive")
    private Double amountPaid;

    @NotNull(message = "Payment method is required")
    private EPaymentMethod paymentMethod;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    private String transactionReference;
}