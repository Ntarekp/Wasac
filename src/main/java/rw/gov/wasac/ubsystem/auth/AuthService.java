package rw.gov.wasac.ubsystem.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import rw.gov.wasac.ubsystem.enums.ERole;
import rw.gov.wasac.ubsystem.enums.EStatus;
import rw.gov.wasac.ubsystem.exception.BadRequestException;
import rw.gov.wasac.ubsystem.security.CustomUserDetailsService;
import rw.gov.wasac.ubsystem.security.JwtUtil;
import rw.gov.wasac.ubsystem.user.User;
import rw.gov.wasac.ubsystem.user.UserRepository;
import rw.gov.wasac.ubsystem.user.UserService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getStatus() == EStatus.INACTIVE) {
            throw new BadRequestException("Account is inactive. Contact administrator.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return LoginResponse.builder()
                .userId(user.getId())
                .fullNames(user.getFullNames())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .build();
    }

    /**
     * Public self-registration: always assigns ROLE_CUSTOMER.
     * Admins use POST /api/users to create privileged accounts.
     */
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use: " + request.getEmail());
        }
        User user = User.builder()
                .fullNames(request.getFullNames())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(userService.encodePassword(request.getPassword()))
                .role(ERole.ROLE_CUSTOMER)
                .status(EStatus.ACTIVE)
                .build();
        return userRepository.save(user);
    }
}