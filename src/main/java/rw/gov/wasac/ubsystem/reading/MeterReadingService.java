package rw.gov.wasac.ubsystem.reading;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rw.gov.wasac.ubsystem.exception.BadRequestException;
import rw.gov.wasac.ubsystem.exception.ResourceNotFoundException;
import rw.gov.wasac.ubsystem.meter.Meter;
import rw.gov.wasac.ubsystem.meter.MeterService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeterReadingService {

    private final MeterReadingRepository readingRepository;
    private final MeterService meterService;

    public MeterReading captureReading(MeterReadingDTO dto) {
        Meter meter = meterService.getMeterById(dto.getMeterId());

        meterService.validateMeterActive(dto.getMeterId());

        int month = dto.getReadingDate().getMonthValue();
        int year  = dto.getReadingDate().getYear();

        if (readingRepository.existsByMeterIdAndReadingMonthAndReadingYear(dto.getMeterId(), month, year)) {
            throw new BadRequestException(
                    "A reading already exists for meter " + meter.getMeterNumber() +
                            " for " + month + "/" + year);
        }

        double expectedPrevious = readingRepository
                .findTopByMeterIdOrderByReadingDateDesc(dto.getMeterId())
                .map(MeterReading::getCurrentReading)
                .orElse(0.0);

        if (!dto.getPreviousReading().equals(expectedPrevious)) {
            throw new BadRequestException(
                    "Previous reading (" + dto.getPreviousReading() +
                            ") does not match the last recorded reading (" + expectedPrevious + ")");
        }

        if (dto.getCurrentReading() <= dto.getPreviousReading()) {
            throw new BadRequestException(
                    "Current reading (" + dto.getCurrentReading() +
                            ") must be greater than previous reading (" + dto.getPreviousReading() + ")");
        }

        double consumption = dto.getCurrentReading() - dto.getPreviousReading();

        MeterReading reading = MeterReading.builder()
                .meter(meter)
                .previousReading(dto.getPreviousReading())
                .currentReading(dto.getCurrentReading())
                .readingDate(dto.getReadingDate())
                .readingMonth(month)
                .readingYear(year)
                .consumption(consumption)
                .build();

        return readingRepository.save(reading);
    }

    public List<MeterReading> getAllReadings() {
        return readingRepository.findAll();
    }

    public MeterReading getReadingById(UUID id) {
        return readingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reading not found: " + id));
    }

    public List<MeterReading> getReadingsByMeter(UUID meterId) {
        return readingRepository.findByMeterId(meterId);
    }
}
