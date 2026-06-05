package rw.gov.wasac.ubsystem.tariff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rw.gov.wasac.ubsystem.enums.EMeterType;

import java.util.Optional;
import java.util.UUID;

public interface TariffRepository extends JpaRepository<Tariff, UUID> {

    Optional<Tariff> findTopByMeterTypeAndActiveTrueOrderByVersionDesc(EMeterType meterType);

    @Query("SELECT COALESCE(MAX(t.version), 0) FROM Tariff t WHERE t.meterType = :meterType")
    int findMaxVersionByMeterType(EMeterType meterType);
}