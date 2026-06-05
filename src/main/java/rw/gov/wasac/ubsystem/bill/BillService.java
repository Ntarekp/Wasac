package rw.gov.wasac.ubsystem.bill;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.wasac.ubsystem.customer.Customer;
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
import java.util.ArrayList;
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

    @Value("${app.messaging.java-fallback:false}")
    private boolean javaMessagingFallback;

    @Transactional
    public Bill generateBill(BillGenerationDTO dto) {
        MeterReading reading = readingRepository.findById(dto.getReadingId())
                .orElseThrow(() -> new ResourceNotFoundException("Reading not found: " + dto.getReadingId()));
        return generateBillFromReading(reading);
    }

    @Transactional
    public BillBatchResultDTO generateMonthlyBills(BillBatchGenerationDTO dto) {
        List<MeterReading> readings = readingRepository.findByReadingMonthAndReadingYear(
                dto.getBillingMonth(), dto.getBillingYear());

        List<Bill> generated = new ArrayList<>();
        int skipped = 0;

        for (MeterReading reading : readings) {
            try {
                generated.add(generateBillFromReading(reading));
            } catch (BadRequestException ex) {
                skipped++;
            }
        }

        return BillBatchResultDTO.builder()
                .billingMonth(dto.getBillingMonth())
                .billingYear(dto.getBillingYear())
                .generatedCount(generated.size())
                .skippedCount(skipped)
                .bills(generated)
                .build();
    }

    private Bill generateBillFromReading(MeterReading reading) {
        Meter meter = reading.getMeter();

        customerService.validateCustomerActive(meter.getCustomer().getId());

        if (billRepository.existsByMeterIdAndBillingMonthAndBillingYear(
                meter.getId(), reading.getReadingMonth(), reading.getReadingYear())) {
            throw new BadRequestException("Bill already generated for this meter for " +
                    reading.getReadingMonth() + "/" + reading.getReadingYear());
        }

        Tariff tariff = tariffService.getTariffForBilling(
                meter.getMeterType(), reading.getReadingMonth(), reading.getReadingYear());
        double totalAmount = tariffService.calculateAmount(tariff, reading.getConsumption());

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
                .penaltyApplied(false)
                .build();

        Bill saved = billRepository.save(bill);
        notifyBillGenerated(saved);
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
        if (bill.getStatus() != EBillStatus.UNPAID) {
            throw new BadRequestException("Only UNPAID bills can be approved");
        }
        bill.setStatus(EBillStatus.APPROVED);
        return billRepository.save(bill);
    }

    public Bill getBillByReference(String ref) {
        return billRepository.findByBillReference(ref)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with reference: " + ref));
    }

    public void validateBillPayable(Bill bill) {
        if (bill.getStatus() == EBillStatus.UNPAID) {
            throw new BadRequestException("Bill must be approved before payment can be recorded");
        }
        if (bill.getStatus() == EBillStatus.PAID) {
            throw new BadRequestException("Bill is already fully paid");
        }
    }

    public Bill updateBillPayment(Bill bill, double amountPaid) {
        EBillStatus previousStatus = bill.getStatus();
        double newPaid = bill.getPaidAmount() + amountPaid;
        double newBalance = bill.getTotalAmount() - newPaid;

        bill.setPaidAmount(newPaid);
        bill.setOutstandingBalance(Math.max(newBalance, 0.0));

        if (newBalance <= 0) {
            bill.setStatus(EBillStatus.PAID);
        } else {
            bill.setStatus(EBillStatus.PARTIALLY_PAID);
        }

        Bill saved = billRepository.save(bill);
        if (saved.getStatus() == EBillStatus.PAID && previousStatus != EBillStatus.PAID) {
            notifyPaymentComplete(saved);
        }
        return saved;
    }

    private void notifyBillGenerated(Bill bill) {
        if (!javaMessagingFallback) {
            return;
        }
        Customer customer = bill.getCustomer();
        messageService.sendBillNotification(
                customer,
                formatBillingPeriod(bill.getBillingMonth(), bill.getBillingYear()),
                bill.getTotalAmount(),
                "BILL_GENERATED");
    }

    private void notifyPaymentComplete(Bill bill) {
        if (!javaMessagingFallback) {
            return;
        }
        Customer customer = bill.getCustomer();
        messageService.sendBillNotification(
                customer,
                formatBillingPeriod(bill.getBillingMonth(), bill.getBillingYear()),
                bill.getTotalAmount(),
                "PAYMENT_COMPLETE");
    }

    private String formatBillingPeriod(int month, int year) {
        String monthName = LocalDate.of(year, month, 1)
                .getMonth()
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return monthName + "/" + year;
    }
}
