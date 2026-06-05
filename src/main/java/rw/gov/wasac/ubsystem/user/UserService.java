package rw.gov.wasac.ubsystem.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.wasac.ubsystem.auth.AuthAccountEmailService;
import rw.gov.wasac.ubsystem.customer.Customer;
import rw.gov.wasac.ubsystem.customer.CustomerService;
import rw.gov.wasac.ubsystem.enums.ERole;
import rw.gov.wasac.ubsystem.enums.EStatus;
import rw.gov.wasac.ubsystem.exception.BadRequestException;
import rw.gov.wasac.ubsystem.exception.ResourceNotFoundException;
import rw.gov.wasac.ubsystem.util.TemporaryPasswordGenerator;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CustomerService customerService;
    private final PasswordEncoder passwordEncoder;
    private final AuthAccountEmailService authAccountEmailService;

    public String encodePassword(String raw) {
        return passwordEncoder.encode(raw);
    }

    @Transactional
    public UserCreationResult createUser(UserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already in use: " + dto.getEmail());
        }

        boolean staffAccount = dto.getRole() != ERole.ROLE_CUSTOMER;
        boolean passwordBlank = dto.getPassword() == null || dto.getPassword().isBlank();
        String rawPassword = passwordBlank ? TemporaryPasswordGenerator.generate(12) : dto.getPassword();
        boolean mustChangePassword = staffAccount || passwordBlank;
        boolean sendCredentialsEmail = staffAccount || passwordBlank;

        Customer customer = resolveCustomerLink(dto.getRole(), dto.getCustomerId(), dto.getEmail());

        User user = User.builder()
                .fullNames(dto.getFullNames())
                .email(dto.getEmail().trim().toLowerCase())
                .phoneNumber(dto.getPhoneNumber())
                .password(passwordEncoder.encode(rawPassword))
                .role(dto.getRole())
                .customer(customer)
                .status(EStatus.ACTIVE)
                .mustChangePassword(mustChangePassword)
                .emailVerified(true)
                .build();

        User saved = userRepository.save(user);

        if (sendCredentialsEmail) {
            authAccountEmailService.sendWelcomeCredentials(
                    saved.getEmail(), saved.getFullNames(), rawPassword, saved.getRole());
        }

        return new UserCreationResult(saved, sendCredentialsEmail ? rawPassword : null);
    }

    Customer resolveCustomerLink(ERole role, UUID customerId, String email) {
        if (role != ERole.ROLE_CUSTOMER) {
            return null;
        }
        if (customerId != null) {
            return customerService.getCustomerById(customerId);
        }
        return customerService.findByEmail(email).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public User updateUser(UUID id, UserUpdateDTO dto) {
        User user = getUserById(id);
        user.setFullNames(dto.getFullNames());
        user.setPhoneNumber(dto.getPhoneNumber());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setMustChangePassword(true);
        }

        if (user.getRole() == ERole.ROLE_CUSTOMER && dto.getCustomerId() != null) {
            user.setCustomer(customerService.getCustomerById(dto.getCustomerId()));
        }

        return userRepository.save(user);
    }

    public User updateStatus(UUID id, EStatus status) {
        User user = getUserById(id);
        user.setStatus(status);
        return userRepository.save(user);
    }

    public void deleteUser(UUID id, UUID currentUserId) {
        if (id.equals(currentUserId)) {
            throw new BadRequestException("You cannot delete your own account.");
        }

        User target = getUserById(id);

        if (target.getRole() == ERole.ROLE_ADMIN && target.getStatus() == EStatus.ACTIVE) {
            long activeAdmins = userRepository.countByRoleAndStatus(ERole.ROLE_ADMIN, EStatus.ACTIVE);
            if (activeAdmins <= 1) {
                throw new BadRequestException("Cannot delete the last active administrator.");
            }
        }

        userRepository.delete(target);
    }

    public record UserCreationResult(User user, String temporaryPassword) {}
}
