package rw.gov.wasac.ubsystem.meter;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.gov.wasac.ubsystem.enums.EStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeterRepository extends JpaRepository<Meter, UUID> {
    boolean existsByMeterNumber(String meterNumber);
    Optional<Meter> findByMeterNumber(String meterNumber);
    List<Meter> findByCustomerId(UUID customerId);
    List<Meter> findByStatus(EStatus status);
}