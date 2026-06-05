package rw.gov.wasac.ubsystem.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rw.gov.wasac.ubsystem.enums.ERole;
import rw.gov.wasac.ubsystem.enums.EStatus;
import rw.gov.wasac.ubsystem.user.User;
import rw.gov.wasac.ubsystem.user.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("System Admin", "admin@wasac.rw", "+250780000001", ERole.ROLE_ADMIN);
        seedUser("Finance Officer", "finance@wasac.rw", "+250780000002", ERole.ROLE_FINANCE);
        seedUser("Field Operator", "operator@wasac.rw", "+250780000003", ERole.ROLE_OPERATOR);
        seedUser("Test Customer", "customer@wasac.rw", "+250780000004", ERole.ROLE_CUSTOMER);
        log.info("✅ Data seeding complete");
    }

    private void seedUser(String name, String email, String phone, ERole role) {
        if (!userRepository.existsByEmail(email)) {
            User user = User.builder()
                    .fullNames(name)
                    .email(email)
                    .phoneNumber(phone)
                    .password(passwordEncoder.encode("Password@123"))
                    .role(role)
                    .status(EStatus.ACTIVE)
                    .build();
            userRepository.save(user);
            log.info("Seeded user: {} ({})", email, role);
        }
    }
}