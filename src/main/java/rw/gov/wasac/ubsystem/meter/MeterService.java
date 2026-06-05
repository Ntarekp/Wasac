package rw.gov.wasac.ubsystem.meter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rw.gov.wasac.ubsystem.customer.Customer;
import rw.gov.wasac.ubsystem.customer.CustomerService;
import rw.gov.wasac.ubsystem.enums.EStatus;
import rw.gov.wasac.ubsystem.exception.BadRequestException;
import rw.gov.wasac.ubsystem.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeterService {

    private final MeterRepository meterRepository;
    private final CustomerService customerService;

    public Meter createMeter(MeterDTO dto) {
        if (meterRepository.existsByMeterNumber(dto.getMeterNumber())) {
            throw new BadRequestException("Meter number already exists: " + dto.getMeterNumber());
        }
        Customer customer = customerService.getCustomerById(dto.getCustomerId());
        Meter meter = Meter.builder()
                .meterNumber(dto.getMeterNumber())
                .meterType(dto.getMeterType())
                .installationDate(dto.getInstallationDate())
                .status(EStatus.ACTIVE)
                .customer(customer)
                .build();
        return meterRepository.save(meter);
    }

    public List<Meter> getAllMeters() {
        return meterRepository.findAll();
    }

    public Meter getMeterById(UUID id) {
        return meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found: " + id));
    }

    public List<Meter> getMetersByCustomer(UUID customerId) {
        return meterRepository.findByCustomerId(customerId);
    }

    public Meter updateStatus(UUID id, EStatus status) {
        Meter meter = getMeterById(id);
        meter.setStatus(status);
        return meterRepository.save(meter);
    }

    public void validateMeterActive(UUID meterId) {
        Meter m = getMeterById(meterId);
        if (m.getStatus() == EStatus.INACTIVE) {
            throw new BadRequestException("Meter is inactive: " + m.getMeterNumber());
        }
    }
}