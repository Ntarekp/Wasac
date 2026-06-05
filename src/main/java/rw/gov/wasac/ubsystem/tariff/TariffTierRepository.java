package rw.gov.wasac.ubsystem.tariff;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TariffTierRepository extends JpaRepository<TariffTier, UUID> {
    List<TariffTier> findByTariffIdOrderByFromUnit(UUID tariffId);
}