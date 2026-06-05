package rw.gov.wasac.ubsystem.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rw.gov.wasac.ubsystem.enums.EStatus;
import rw.gov.wasac.ubsystem.exception.BadRequestException;
import rw.gov.wasac.ubsystem.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String encodePassword(String raw) {
        return passwordEncoder.encode(raw);
    }

    public User createUser(UserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already in use: " + dto.getEmail());
        }
        User user = User.builder()
                .fullNames(dto.getFullNames())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .status(EStatus.ACTIVE)
                .build();
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public User updateStatus(UUID id, EStatus status) {
        User user = getUserById(id);
        user.setStatus(status);
        return userRepository.save(user);
    }

    public void deleteUser(UUID id) {
        userRepository.delete(getUserById(id));
    }
}