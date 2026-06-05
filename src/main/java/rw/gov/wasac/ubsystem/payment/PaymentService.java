package rw.gov.wasac.ubsystem.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.wasac.ubsystem.bill.Bill;
import rw.gov.wasac.ubsystem.bill.BillService;
import rw.gov.wasac.ubsystem.enums.EBillStatus;
import rw.gov.wasac.ubsystem.exception.BadRequestException;
import rw.gov.wasac.ubsystem.exception.ResourceNotFoundException;
import rw.gov.wasac.ubsystem.message.MessageService;

import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillService billService;
    private final MessageService messageService;

    @Transactional
    public Payment recordPayment(PaymentDTO dto) {
        Bill bill = billService.getBillByReference(dto.getBillReference());

        if (bill.getStatus() == EBillStatus.PAID) {
            throw new BadRequestException("Bill is already fully paid");
        }

        if (dto.getAmountPaid() > bill.getOutstandingBalance()) {
            throw new BadRequestException(
                    "Payment amount (" + dto.getAmountPaid() +
                            ") exceeds outstanding balance (" + bill.getOutstandingBalance() + ")"
            );
        }

        Payment payment = Payment.builder()
                .bill(bill)
                .amountPaid(dto.getAmountPaid())
                .paymentMethod(dto.getPaymentMethod())
                .paymentDate(dto.getPaymentDate())
                .transactionReference(dto.getTransactionReference())
                .build();

        Payment saved = paymentRepository.save(payment);

        // Update bill balance
        Bill updatedBill = billService.updateBillPayment(bill, dto.getAmountPaid());

        // Notify on full payment
        if (updatedBill.getStatus() == EBillStatus.PAID) {
            String monthYear = java.time.Month.of(bill.getBillingMonth())
                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH) + "/" + bill.getBillingYear();
            messageService.sendBillNotification(
                    bill.getCustomer(),
                    monthYear,
                    bill.getTotalAmount(),
                    "PAYMENT_COMPLETE"
            );
        }

        return saved;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
    }

    public List<Payment> getPaymentsByBill(UUID billId) {
        return paymentRepository.findByBillId(billId);
    }

    public List<Payment> getPaymentsByCustomer(UUID customerId) {
        return paymentRepository.findByBillCustomerId(customerId);
    }
}