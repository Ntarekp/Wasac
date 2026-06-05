package rw.gov.wasac.ubsystem.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import rw.gov.wasac.ubsystem.bill.Bill;
import rw.gov.wasac.ubsystem.enums.EPaymentMethod;
import rw.gov.wasac.ubsystem.enums.EPaymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "meterReading", "meter", "tariff"})
    private Bill bill;

    @Column(nullable = false)
    private Double amountPaid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EPaymentMethod paymentMethod;

    @Column(nullable = false)
    private LocalDate paymentDate;

    private String transactionReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EPaymentStatus status;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = EPaymentStatus.PENDING;
    }
}