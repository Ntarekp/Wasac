package rw.gov.wasac.ubsystem.reading;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.gov.wasac.ubsystem.customer.Customer;
import rw.gov.wasac.ubsystem.enums.EStatus;
import rw.gov.wasac.ubsystem.enums.EMeterType;
import rw.gov.wasac.ubsystem.exception.BadRequestException;
import rw.gov.wasac.ubsystem.meter.Meter;
import rw.gov.wasac.ubsystem.meter.MeterService;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeterReadingServiceTest {

    @Mock private MeterReadingRepository readingRepository;
    @Mock private MeterService meterService;
    @InjectMocks private MeterReadingService readingService;

    @Test
    void captureReading_rejectsWhenCurrentNotGreaterThanPrevious() {
        UUID meterId = UUID.randomUUID();
        Meter meter = activeMeter(meterId);

        when(meterService.getMeterById(meterId)).thenReturn(meter);
        doNothing().when(meterService).validateMeterActive(meterId);
        when(readingRepository.existsByMeterIdAndReadingMonthAndReadingYear(meterId, 6, 2025)).thenReturn(false);
        when(readingRepository.findTopByMeterIdOrderByReadingDateDesc(meterId)).thenReturn(Optional.empty());

        MeterReadingDTO dto = new MeterReadingDTO();
        dto.setMeterId(meterId);
        dto.setPreviousReading(0.0);
        dto.setCurrentReading(0.0);
        dto.setReadingDate(LocalDate.of(2025, 6, 5));

        assertThrows(BadRequestException.class, () -> readingService.captureReading(dto));
        verify(readingRepository, never()).save(any());
    }

    @Test
    void captureReading_rejectsMismatchedPreviousReading() {
        UUID meterId = UUID.randomUUID();
        Meter meter = activeMeter(meterId);

        when(meterService.getMeterById(meterId)).thenReturn(meter);
        doNothing().when(meterService).validateMeterActive(meterId);
        when(readingRepository.existsByMeterIdAndReadingMonthAndReadingYear(meterId, 6, 2025)).thenReturn(false);
        when(readingRepository.findTopByMeterIdOrderByReadingDateDesc(meterId))
                .thenReturn(Optional.of(MeterReading.builder().currentReading(50.0).build()));

        MeterReadingDTO dto = new MeterReadingDTO();
        dto.setMeterId(meterId);
        dto.setPreviousReading(10.0);
        dto.setCurrentReading(60.0);
        dto.setReadingDate(LocalDate.of(2025, 6, 5));

        assertThrows(BadRequestException.class, () -> readingService.captureReading(dto));
    }

    private Meter activeMeter(UUID meterId) {
        return Meter.builder()
                .id(meterId)
                .meterNumber("MTR-001")
                .meterType(EMeterType.WATER)
                .status(EStatus.ACTIVE)
                .customer(Customer.builder().id(UUID.randomUUID()).build())
                .build();
    }
}
