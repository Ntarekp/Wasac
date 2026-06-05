package rw.gov.wasac.ubsystem.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import rw.gov.wasac.ubsystem.bill.Bill;
import rw.gov.wasac.ubsystem.customer.Customer;
import rw.gov.wasac.ubsystem.enums.ERole;
import rw.gov.wasac.ubsystem.exception.BadRequestException;
import rw.gov.wasac.ubsystem.meter.Meter;
import rw.gov.wasac.ubsystem.payment.Payment;
import rw.gov.wasac.ubsystem.user.User;
import rw.gov.wasac.ubsystem.user.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("User not found"));
    }

    public boolean isCustomerRole() {
        User user = getCurrentUser();
        return user.getRole() == ERole.ROLE_CUSTOMER;
    }

    public void verifyCustomerAccess(UUID customerId) {
        if (!isCustomerRole()) {
            return;
        }
        User user = getCurrentUser();
        Customer linked = user.getCustomer();
        if (linked == null || !linked.getId().equals(customerId)) {
            throw new AccessDeniedException("You can only access your own customer records");
        }
    }

    public void verifyBillAccess(Bill bill) {
        verifyCustomerAccess(bill.getCustomer().getId());
    }

    public void verifyMeterAccess(Meter meter) {
        verifyCustomerAccess(meter.getCustomer().getId());
    }

    public void verifyPaymentAccess(Payment payment) {
        verifyCustomerAccess(payment.getBill().getCustomer().getId());
    }

    public UUID requireLinkedCustomerId() {
        User user = getCurrentUser();
        if (user.getCustomer() == null) {
            throw new BadRequestException("Customer account is not linked to a customer profile. Contact administrator.");
        }
        return user.getCustomer().getId();
    }
}
