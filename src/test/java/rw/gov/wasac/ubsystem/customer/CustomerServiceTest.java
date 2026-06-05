package rw.gov.wasac.ubsystem.customer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.gov.wasac.ubsystem.enums.EStatus;
import rw.gov.wasac.ubsystem.exception.BadRequestException;
import rw.gov.wasac.ubsystem.meter.MeterRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private MeterRepository meterRepository;
    @InjectMocks private CustomerService customerService;

    @Test
    void updateCustomer_rejectsDuplicateNationalId() {
        UUID id = UUID.randomUUID();
        Customer existing = Customer.builder().id(id).nationalId("OLD-ID").email("a@test.rw").status(EStatus.ACTIVE).build();
        CustomerDTO dto = new CustomerDTO();
        dto.setFullNames("Test");
        dto.setNationalId("NEW-ID");
        dto.setEmail("a@test.rw");
        dto.setPhoneNumber("+250780000000");
        dto.setAddress("Kigali");

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(customerRepository.existsByNationalIdAndIdNot("NEW-ID", id)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> customerService.updateCustomer(id, dto));
    }

    @Test
    void deleteCustomer_rejectsWhenMetersExist() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.of(
                Customer.builder().id(id).status(EStatus.ACTIVE).build()));
        when(meterRepository.existsByCustomerId(id)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> customerService.deleteCustomer(id));
        verify(customerRepository, never()).delete(any());
    }
}
