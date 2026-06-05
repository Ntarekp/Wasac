package rw.gov.wasac.ubsystem.tariff;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.gov.wasac.ubsystem.enums.EMeterType;
import rw.gov.wasac.ubsystem.enums.ETariffType;
import rw.gov.wasac.ubsystem.exception.BadRequestException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TariffServiceTest {

    @Mock private TariffRepository tariffRepository;
    @Mock private TariffTierRepository tierRepository;
    @InjectMocks private TariffService tariffService;

    @Test
    void createTariff_rejectsOverlappingTiers() {
        TariffDTO dto = baseTierTariff();
        TariffDTO.TierDTO t1 = new TariffDTO.TierDTO();
        t1.setFromUnit(0.0);
        t1.setToUnit(10.0);
        t1.setPricePerUnit(100.0);
        TariffDTO.TierDTO t2 = new TariffDTO.TierDTO();
        t2.setFromUnit(5.0);
        t2.setToUnit(20.0);
        t2.setPricePerUnit(150.0);
        dto.setTiers(List.of(t1, t2));

        assertThrows(BadRequestException.class, () -> tariffService.createTariff(dto));
        verify(tariffRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createTariff_rejectsInvalidTierRange() {
        TariffDTO dto = baseTierTariff();
        TariffDTO.TierDTO t1 = new TariffDTO.TierDTO();
        t1.setFromUnit(10.0);
        t1.setToUnit(5.0);
        t1.setPricePerUnit(100.0);
        dto.setTiers(List.of(t1));

        assertThrows(BadRequestException.class, () -> tariffService.createTariff(dto));
    }

    private TariffDTO baseTierTariff() {
        TariffDTO dto = new TariffDTO();
        dto.setName("Water Tier");
        dto.setMeterType(EMeterType.WATER);
        dto.setTariffType(ETariffType.TIER_BASED);
        dto.setServiceCharge(500.0);
        dto.setVatPercentage(18.0);
        dto.setLatePaymentPenaltyPercentage(5.0);
        dto.setEffectiveFrom(LocalDate.now());
        return dto;
    }
}
