package rw.gov.wasac.ubsystem.tariff;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.wasac.ubsystem.enums.EMeterType;
import rw.gov.wasac.ubsystem.enums.ETariffType;
import rw.gov.wasac.ubsystem.exception.BadRequestException;
import rw.gov.wasac.ubsystem.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TariffService {

    private final TariffRepository tariffRepository;
    private final TariffTierRepository tierRepository;

    @Transactional
    public Tariff createTariff(TariffDTO dto) {
        if (dto.getTariffType() == ETariffType.FLAT_RATE && dto.getUnitPrice() == null) {
            throw new BadRequestException("Unit price is required for FLAT_RATE tariff");
        }
        if (dto.getTariffType() == ETariffType.TIER_BASED &&
                (dto.getTiers() == null || dto.getTiers().isEmpty())) {
            throw new BadRequestException("Tiers are required for TIER_BASED tariff");
        }

        // Deactivate current active tariff of same meter type
        tariffRepository.findTopByMeterTypeAndActiveTrueOrderByVersionDesc(dto.getMeterType())
                .ifPresent(old -> {
                    old.setActive(false);
                    tariffRepository.save(old);
                });

        int version = tariffRepository.findMaxVersionByMeterType(dto.getMeterType()) + 1;

        Tariff tariff = Tariff.builder()
                .name(dto.getName())
                .version(version)
                .meterType(dto.getMeterType())
                .tariffType(dto.getTariffType())
                .unitPrice(dto.getUnitPrice())
                .serviceCharge(dto.getServiceCharge())
                .vatPercentage(dto.getVatPercentage())
                .latePaymentPenaltyPercentage(dto.getLatePaymentPenaltyPercentage())
                .effectiveFrom(dto.getEffectiveFrom())
                .active(true)
                .build();

        Tariff saved = tariffRepository.save(tariff);

        if (dto.getTariffType() == ETariffType.TIER_BASED) {
            for (TariffDTO.TierDTO tier : dto.getTiers()) {
                tierRepository.save(TariffTier.builder()
                        .tariff(saved)
                        .fromUnit(tier.getFromUnit())
                        .toUnit(tier.getToUnit())
                        .pricePerUnit(tier.getPricePerUnit())
                        .build());
            }
        }
        return saved;
    }

    public List<Tariff> getAllTariffs() {
        return tariffRepository.findAll();
    }

    public Tariff getTariffById(UUID id) {
        return tariffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tariff not found: " + id));
    }

    public Tariff getActiveTariff(EMeterType meterType) {
        return tariffRepository.findTopByMeterTypeAndActiveTrueOrderByVersionDesc(meterType)
                .orElseThrow(() -> new ResourceNotFoundException("No active tariff found for " + meterType));
    }

    public List<TariffTier> getTiersForTariff(UUID tariffId) {
        return tierRepository.findByTariffIdOrderByFromUnit(tariffId);
    }

    public double calculateAmount(EMeterType meterType, double consumption) {
        Tariff tariff = getActiveTariff(meterType);
        double base;

        if (tariff.getTariffType() == ETariffType.FLAT_RATE) {
            if (tariff.getUnitPrice() == null) {
                throw new BadRequestException("Flat rate tariff has no unit price configured");
            }
            base = consumption * tariff.getUnitPrice();
        } else {
            List<TariffTier> tiers = getTiersForTariff(tariff.getId());
            if (tiers.isEmpty()) {
                throw new BadRequestException("Tier-based tariff has no tiers configured");
            }
            base = 0.0;
            double remaining = consumption;
            for (TariffTier tier : tiers) {
                if (remaining <= 0) break;
                double tierRange = tier.getToUnit() - tier.getFromUnit();
                double unitsInTier = Math.min(remaining, tierRange);
                base += unitsInTier * tier.getPricePerUnit();
                remaining -= unitsInTier;
            }
        }

        double withServiceCharge = base + tariff.getServiceCharge();
        double vat = withServiceCharge * (tariff.getVatPercentage() / 100.0);
        return Math.round((withServiceCharge + vat) * 100.0) / 100.0;
    }
}