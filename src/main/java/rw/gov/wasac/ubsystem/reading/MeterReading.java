package rw.gov.wasac.ubsystem.reading;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import rw.gov.wasac.ubsystem.meter.Meter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "meter_readings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"meter_id", "reading_month", "reading_year"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MeterReading {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "customer"})
    private Meter meter;

    @Column(nullable = false)
    private Double previousReading;

    @Column(nullable = false)
    private Double currentReading;

    @Column(nullable = false)
    private LocalDate readingDate;

    @Column(nullable = false)
    private Integer readingMonth;

    @Column(nullable = false)
    private Integer readingYear;

    @Column(nullable = false)
    private Double consumption;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}