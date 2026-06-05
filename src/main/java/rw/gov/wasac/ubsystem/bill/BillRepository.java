package rw.gov.wasac.ubsystem.bill;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.gov.wasac.ubsystem.enums.EBillStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillRepository extends JpaRepository<Bill, UUID> {
    List<Bill> findByCustomerId(UUID customerId);
    List<Bill> findByStatus(EBillStatus status);
    Optional<Bill> findByBillReference(String billReference);
    boolean existsByMeterIdAndBillingMonthAndBillingYear(UUID meterId, int month, int year);
}