package rw.gov.wasac.ubsystem.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rw.gov.wasac.ubsystem.auth.AuthAccountEmailService;
import rw.gov.wasac.ubsystem.enums.ERole;
import rw.gov.wasac.ubsystem.enums.EStatus;
import rw.gov.wasac.ubsystem.user.User;
import rw.gov.wasac.ubsystem.user.UserRepository;
import rw.gov.wasac.ubsystem.util.TemporaryPasswordGenerator;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final List<String> LEGACY_SEED_EMAILS = List.of(
            "admin@wasac.rw",
            "finance@wasac.rw",
            "operator@wasac.rw",
            "customer@wasac.rw"
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthAccountEmailService authAccountEmailService;

    @Override
    public void run(String... args) {
        removeLegacySeedUsers();
        seedUser("System Admin", "kpntare@gmail.com", "+250780000001", ERole.ROLE_ADMIN);
        seedUser("Finance Officer", "benmu91@gmail.com", "+250780000002", ERole.ROLE_FINANCE);
        seedUser("Field Operator", "Cabledie@gmail.com", "+250780000003", ERole.ROLE_OPERATOR);
        seedUser("Test Customer", "devroom210@gmail.com", "+250780000004", ERole.ROLE_CUSTOMER);
        log.info("Data seeding complete");
    }

    private void removeLegacySeedUsers() {
        for (String email : LEGACY_SEED_EMAILS) {
            userRepository.findByEmail(email).ifPresent(user -> {
                userRepository.delete(user);
                log.info("Removed legacy seed user: {}", email);
            });
        }
    }

    private void seedUser(String name, String email, String phone, ERole role) {
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            return;
        }

        String temporaryPassword = TemporaryPasswordGenerator.generate(12);
        User user = User.builder()
                .fullNames(name)
                .email(normalizedEmail)
                .phoneNumber(phone)
                .password(passwordEncoder.encode(temporaryPassword))
                .role(role)
                .status(EStatus.ACTIVE)
                .mustChangePassword(true)
                .emailVerified(true)
                .build();
        userRepository.save(user);
        log.info("Seeded user: {} ({})", normalizedEmail, role);

        if (authAccountEmailService.isEmailAvailable()) {
            try {
                authAccountEmailService.sendWelcomeCredentials(normalizedEmail, name, temporaryPassword, role);
                log.info("Welcome email sent to {}", normalizedEmail);
            } catch (Exception ex) {
                log.warn("Could not email seeded user {}: {}", normalizedEmail, ex.getMessage());
            }
        } else {
            log.warn("Email not configured — seeded password for {} is logged only in dev (check mail settings)", normalizedEmail);
            log.info("DEV ONLY temporary password for {}: {}", normalizedEmail, temporaryPassword);
        }
    }
}
