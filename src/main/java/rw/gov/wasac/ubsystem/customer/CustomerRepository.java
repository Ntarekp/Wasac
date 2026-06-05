package rw.gov.wasac.ubsystem.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.gov.wasac.ubsystem.enums.EStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    boolean existsByNationalId(String nationalId);
    boolean existsByEmail(String email);
    Optional<Customer> findByEmail(String email);
    List<Customer> findByStatus(EStatus status);
}