package rw.gov.wasac.ubsystem.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.wasac.ubsystem.bill.Bill;
import rw.gov.wasac.ubsystem.bill.BillService;
import rw.gov.wasac.ubsystem.enums.EPaymentStatus;
import rw.gov.wasac.ubsystem.exception.BadRequestException;
import rw.gov.wasac.ubsystem.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillService billService;

    @Transactional
    public Payment recordPayment(PaymentDTO dto) {
        Bill bill = billService.getBillByReference(dto.getBillReference());
        billService.validateBillPayable(bill);

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
                .status(EPaymentStatus.PENDING)
                .build();

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment approvePayment(UUID paymentId) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != EPaymentStatus.PENDING) {
            throw new BadRequestException("Only PENDING payments can be approved");
        }

        Bill bill = payment.getBill();
        billService.validateBillPayable(bill);

        if (payment.getAmountPaid() > bill.getOutstandingBalance()) {
            throw new BadRequestException(
                    "Payment amount (" + payment.getAmountPaid() +
                            ") exceeds outstanding balance (" + bill.getOutstandingBalance() + ")"
            );
        }

        payment.setStatus(EPaymentStatus.APPROVED);
        paymentRepository.save(payment);

        billService.updateBillPayment(bill, payment.getAmountPaid());
        return payment;
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
