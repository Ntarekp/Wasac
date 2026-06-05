package rw.gov.wasac.ubsystem.bill;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.wasac.ubsystem.customer.CustomerService;
import rw.gov.wasac.ubsystem.enums.EBillStatus;
import rw.gov.wasac.ubsystem.exception.BadRequestException;
import rw.gov.wasac.ubsystem.exception.ResourceNotFoundException;
import rw.gov.wasac.ubsystem.message.MessageService;
import rw.gov.wasac.ubsystem.meter.Meter;
import rw.gov.wasac.ubsystem.reading.MeterReading;
import rw.gov.wasac.ubsystem.reading.MeterReadingRepository;
import rw.gov.wasac.ubsystem.tariff.Tariff;
import rw.gov.wasac.ubsystem.tariff.TariffService;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final MeterReadingRepository readingRepository;
    private final TariffService tariffService;
    private final CustomerService customerService;
    private final MessageService messageService;

    @Transactional
    public Bill generateBill(BillGenerationDTO dto) {
        MeterReading reading = readingRepository.findById(dto.getReadingId())
                .orElseThrow(() -> new ResourceNotFoundException("Reading not found: " + dto.getReadingId()));

        Meter meter = reading.getMeter();

        // Validate customer is active
        customerService.validateCustomerActive(meter.getCustomer().getId());

        // Check no duplicate bill
        if (billRepository.existsByMeterIdAndBillingMonthAndBillingYear(
                meter.getId(), reading.getReadingMonth(), reading.getReadingYear())) {
            throw new BadRequestException("Bill already generated for this meter for " +
                    reading.getReadingMonth() + "/" + reading.getReadingYear());
        }

        Tariff tariff = tariffService.getActiveTariff(meter.getMeterType());
        double totalAmount = tariffService.calculateAmount(meter.getMeterType(), reading.getConsumption());

        String ref = "BILL-" + meter.getMeterNumber() + "-" +
                reading.getReadingYear() + String.format("%02d", reading.getReadingMonth());

        Bill bill = Bill.builder()
                .billReference(ref)
                .customer(meter.getCustomer())
                .meter(meter)
                .meterReading(reading)
                .tariff(tariff)
                .billingMonth(reading.getReadingMonth())
                .billingYear(reading.getReadingYear())
                .consumption(reading.getConsumption())
                .totalAmount(totalAmount)
                .paidAmount(0.0)
                .outstandingBalance(totalAmount)
                .status(EBillStatus.UNPAID)
                .dueDate(LocalDate.now().plusDays(30))
                .build();

        Bill saved = billRepository.save(bill);

        // Notify customer on bill generation
        String monthName = LocalDate.of(reading.getReadingYear(), reading.getReadingMonth(), 1)
                .getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        messageService.sendBillNotification(
                meter.getCustomer(),
                monthName + "/" + reading.getReadingYear(),
                totalAmount,
                "BILL_GENERATED"
        );

        return saved;
    }

    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    public Bill getBillById(UUID id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + id));
    }

    public List<Bill> getBillsByCustomer(UUID customerId) {
        return billRepository.findByCustomerId(customerId);
    }

    public Bill approveBill(UUID id) {
        Bill bill = getBillById(id);
        if (bill.getStatus() == EBillStatus.PAID) {
            throw new BadRequestException("Bill is already paid");
        }
        bill.setStatus(EBillStatus.APPROVED);
        return billRepository.save(bill);
    }

    public Bill getBillByReference(String ref) {
        return billRepository.findByBillReference(ref)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with reference: " + ref));
    }

    // Called internally by payment service
    public Bill updateBillPayment(Bill bill, double amountPaid) {
        double newPaid = bill.getPaidAmount() + amountPaid;
        double newBalance = bill.getTotalAmount() - newPaid;

        bill.setPaidAmount(newPaid);
        bill.setOutstandingBalance(Math.max(newBalance, 0.0));

        if (newBalance <= 0) {
            bill.setStatus(EBillStatus.PAID);
        } else if (newPaid > 0) {
            bill.setStatus(EBillStatus.PARTIALLY_PAID);
        }
        return billRepository.save(bill);
    }
}