package rw.gov.wasac.ubsystem.bill;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.gov.wasac.ubsystem.customer.Customer;
import rw.gov.wasac.ubsystem.customer.CustomerService;
import rw.gov.wasac.ubsystem.enums.EBillStatus;
import rw.gov.wasac.ubsystem.exception.BadRequestException;
import rw.gov.wasac.ubsystem.message.MessageService;
import rw.gov.wasac.ubsystem.reading.MeterReadingRepository;
import rw.gov.wasac.ubsystem.tariff.TariffService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock private BillRepository billRepository;
    @Mock private MeterReadingRepository readingRepository;
    @Mock private TariffService tariffService;
    @Mock private CustomerService customerService;
    @Mock private MessageService messageService;
    @InjectMocks private BillService billService;

    @Test
    void validateBillPayable_rejectsUnpaidBill() {
        Bill bill = Bill.builder()
                .status(EBillStatus.UNPAID)
                .customer(Customer.builder().id(UUID.randomUUID()).build())
                .build();

        assertThrows(BadRequestException.class, () -> billService.validateBillPayable(bill));
    }

    @Test
    void approveBill_rejectsNonUnpaidBill() {
        UUID billId = UUID.randomUUID();
        Bill bill = Bill.builder().id(billId).status(EBillStatus.APPROVED).build();

        org.mockito.Mockito.when(billRepository.findById(billId)).thenReturn(java.util.Optional.of(bill));

        assertThrows(BadRequestException.class, () -> billService.approveBill(billId));
    }
}
