package rw.gov.wasac.ubsystem.tariff;

import jakarta.persistence.*;
import lombok.*;
import rw.gov.wasac.ubsystem.enums.EMeterType;
import rw.gov.wasac.ubsystem.enums.ETariffType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tariffs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tariff {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EMeterType meterType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ETariffType tariffType;

    // For flat rate: price per unit
    private Double unitPrice;

    // Fixed monthly service charge
    @Column(nullable = false)
    private Double serviceCharge;

    // VAT percentage (e.g. 18.0 for 18%)
    @Column(nullable = false)
    private Double vatPercentage;

    // Late payment penalty percentage
    @Column(nullable = false)
    private Double latePaymentPenaltyPercentage;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private Boolean active;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (active == null) active = true;
    }
}