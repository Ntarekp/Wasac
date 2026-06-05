package rw.gov.wasac.ubsystem.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rw.gov.wasac.ubsystem.enums.EStatus;
import rw.gov.wasac.ubsystem.exception.BadRequestException;
import rw.gov.wasac.ubsystem.exception.ResourceNotFoundException;
import rw.gov.wasac.ubsystem.meter.MeterRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final MeterRepository meterRepository;

    public Customer createCustomer(CustomerDTO dto) {
        if (customerRepository.existsByNationalId(dto.getNationalId())) {
            throw new BadRequestException("Customer with this National ID already exists");
        }
        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Customer with this email already exists");
        }
        Customer customer = Customer.builder()
                .fullNames(dto.getFullNames())
                .nationalId(dto.getNationalId())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .address(dto.getAddress())
                .status(EStatus.ACTIVE)
                .build();
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    public java.util.Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Customer updateCustomer(UUID id, CustomerDTO dto) {
        Customer customer = getCustomerById(id);

        if (customerRepository.existsByNationalIdAndIdNot(dto.getNationalId(), id)) {
            throw new BadRequestException("Customer with this National ID already exists");
        }
        if (customerRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new BadRequestException("Customer with this email already exists");
        }

        customer.setFullNames(dto.getFullNames());
        customer.setNationalId(dto.getNationalId());
        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setAddress(dto.getAddress());
        return customerRepository.save(customer);
    }

    public void deleteCustomer(UUID id) {
        Customer customer = getCustomerById(id);
        if (meterRepository.existsByCustomerId(id)) {
            throw new BadRequestException(
                    "Cannot delete customer with installed meters. Deactivate the customer instead.");
        }
        customerRepository.delete(customer);
    }

    public Customer updateStatus(UUID id, EStatus status) {
        Customer customer = getCustomerById(id);
        customer.setStatus(status);
        return customerRepository.save(customer);
    }

    public void validateCustomerActive(UUID customerId) {
        Customer c = getCustomerById(customerId);
        if (c.getStatus() == EStatus.INACTIVE) {
            throw new BadRequestException("Customer is inactive and cannot receive bills");
        }
    }
}