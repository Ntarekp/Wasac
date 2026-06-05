package rw.gov.wasac.ubsystem.tariff;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tariff_tiers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TariffTier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariff tariff;

    @Column(nullable = false)
    private Double fromUnit;

    @Column(nullable = false)
    private Double toUnit;

    @Column(nullable = false)
    private Double pricePerUnit;
}