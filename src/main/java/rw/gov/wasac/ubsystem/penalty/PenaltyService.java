package rw.gov.wasac.ubsystem.penalty;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.wasac.ubsystem.bill.Bill;
import rw.gov.wasac.ubsystem.bill.BillRepository;
import rw.gov.wasac.ubsystem.enums.EBillStatus;
import rw.gov.wasac.ubsystem.tariff.Tariff;
import rw.gov.wasac.ubsystem.tariff.TariffService;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PenaltyService {

    private final BillRepository billRepository;
    private final TariffService tariffService;
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void applyLatePenalties() {
        applyLatePenaltiesNow();
    }

    @Transactional
    public void applyLatePenaltiesNow() {
        try {
            jdbcTemplate.execute("CALL sp_apply_all_late_penalties()");
            log.info("Late penalties applied via stored procedure sp_apply_all_late_penalties");
        } catch (DataAccessException ex) {
            log.warn("Stored procedure unavailable, applying penalties in Java: {}", ex.getMessage());
            applyLatePenaltiesInJava();
        }
    }

    private void applyLatePenaltiesInJava() {
        LocalDate today = LocalDate.now();
        List<Bill> overdueBills = billRepository.findAll().stream()
                .filter(b -> Boolean.FALSE.equals(b.getPenaltyApplied())
                        && b.getDueDate() != null
                        && b.getDueDate().isBefore(today)
                        && (b.getStatus() == EBillStatus.UNPAID
                        || b.getStatus() == EBillStatus.PARTIALLY_PAID
                        || b.getStatus() == EBillStatus.APPROVED))
                .toList();

        for (Bill bill : overdueBills) {
            try {
                Tariff tariff = tariffService.getTariffById(bill.getTariff().getId());
                double penaltyRate = tariff.getLatePaymentPenaltyPercentage() / 100.0;
                double penalty = bill.getOutstandingBalance() * penaltyRate;
                double newTotal = bill.getTotalAmount() + penalty;
                double newBalance = bill.getOutstandingBalance() + penalty;

                bill.setTotalAmount(Math.round(newTotal * 100.0) / 100.0);
                bill.setOutstandingBalance(Math.round(newBalance * 100.0) / 100.0);
                bill.setStatus(EBillStatus.OVERDUE);
                bill.setPenaltyApplied(true);
                billRepository.save(bill);

                log.info("Penalty applied to bill {}: +{}FRW", bill.getBillReference(), penalty);
            } catch (Exception e) {
                log.error("Failed to apply penalty to bill {}: {}", bill.getBillReference(), e.getMessage());
            }
        }
    }

    public List<Bill> getOverdueBills() {
        return billRepository.findByStatus(EBillStatus.OVERDUE);
    }
}
