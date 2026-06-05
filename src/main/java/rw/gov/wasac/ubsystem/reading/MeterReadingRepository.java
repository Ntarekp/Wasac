package rw.gov.wasac.ubsystem.reading;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {
    boolean existsByMeterIdAndReadingMonthAndReadingYear(UUID meterId, int month, int year);
    List<MeterReading> findByMeterId(UUID meterId);
    List<MeterReading> findByReadingMonthAndReadingYear(int month, int year);
    Optional<MeterReading> findTopByMeterIdOrderByReadingDateDesc(UUID meterId);
}