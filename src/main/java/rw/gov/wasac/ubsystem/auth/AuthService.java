package rw.gov.wasac.ubsystem.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.wasac.ubsystem.customer.Customer;
import rw.gov.wasac.ubsystem.customer.CustomerService;
import rw.gov.wasac.ubsystem.enums.EOtpPurpose;
import rw.gov.wasac.ubsystem.enums.ERole;
import rw.gov.wasac.ubsystem.enums.EStatus;
import rw.gov.wasac.ubsystem.exception.BadRequestException;
import rw.gov.wasac.ubsystem.otp.OtpDispatchResult;
import rw.gov.wasac.ubsystem.otp.OtpService;
import rw.gov.wasac.ubsystem.security.CustomUserDetailsService;
import rw.gov.wasac.ubsystem.security.JwtUtil;
import rw.gov.wasac.ubsystem.security.SecurityService;
import rw.gov.wasac.ubsystem.user.User;
import rw.gov.wasac.ubsystem.user.UserRepository;
import rw.gov.wasac.ubsystem.user.UserService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CustomerService customerService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final SecurityService securityService;
    private final PasswordEncoder passwordEncoder;
    private final AuthAccountEmailService authAccountEmailService;

    /**
     * Step 1 of login: validate password and email an OTP. JWT is issued after OTP verification.
     */
    public OtpPendingResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getStatus() == EStatus.INACTIVE) {
            throw new BadRequestException("Account is inactive. Contact administrator.");
        }

        OtpDispatchResult dispatch = otpService.issueOtp(user.getEmail(), EOtpPurpose.LOGIN);
        return toPendingResponse(dispatch);
    }

    @Transactional
    public LoginResponse verifyOtpAndLogin(OtpVerifyDTO request) {
        if (request.getPurpose() != EOtpPurpose.LOGIN) {
            throw new BadRequestException("Use /api/auth/register for email verification during signup.");
        }

        String email = request.getEmail().trim().toLowerCase();
        otpService.verifyOtp(email, request.getCode(), EOtpPurpose.LOGIN);

        User user = userRepository.findByEmail(email)
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
                .customerId(user.getCustomer() != null ? user.getCustomer().getId() : null)
                .mustChangePassword(Boolean.TRUE.equals(user.getMustChangePassword()))
                .emailVerified(Boolean.TRUE.equals(user.getEmailVerified()))
                .token(token)
                .build();
    }

    public OtpPendingResponse requestOtp(OtpRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();

        switch (request.getPurpose()) {
            case EMAIL_VERIFICATION -> {
                if (userRepository.existsByEmail(email)) {
                    throw new BadRequestException("Email already registered. Please login instead.");
                }
            }
            case PASSWORD_RESET -> validatePasswordResetEligible(email);
            case LOGIN -> throw new BadRequestException(
                    "Login OTP is sent automatically after password validation. Use POST /api/auth/login first.");
        }

        OtpDispatchResult dispatch = otpService.issueOtp(email, request.getPurpose());
        return toPendingResponse(dispatch);
    }

    public OtpPendingResponse resendOtp(OtpRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();
        if (request.getPurpose() == EOtpPurpose.PASSWORD_RESET) {
            validatePasswordResetEligible(email);
        }
        if (request.getPurpose() == EOtpPurpose.EMAIL_VERIFICATION && userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered. Please login instead.");
        }

        OtpDispatchResult dispatch = otpService.resendOtp(email, request.getPurpose());
        return toPendingResponse(dispatch);
    }

    /**
     * Step 2 of password reset: verify OTP and set a new password (no JWT required).
     */
    @Transactional
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("No account found for this email."));

        if (user.getStatus() == EStatus.INACTIVE) {
            throw new BadRequestException("Account is inactive. Contact administrator.");
        }

        otpService.verifyOtp(email, request.getOtp(), EOtpPurpose.PASSWORD_RESET);

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from the current password.");
        }

        user.setPassword(userService.encodePassword(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        if (authAccountEmailService.isEmailAvailable()) {
            authAccountEmailService.sendPasswordChanged(user.getEmail(), user.getFullNames());
        }

        return ResetPasswordResponse.builder()
                .email(email)
                .message("Password reset successfully. Login with your new password and complete OTP verification.")
                .build();
    }

    /**
     * Public self-registration: ROLE_CUSTOMER only, requires prior email OTP verification.
     */
    @Transactional
    public User register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already in use: " + email);
        }

        otpService.verifyOtp(email, request.getOtp(), EOtpPurpose.EMAIL_VERIFICATION);

        Customer customer = customerService.findByEmail(email).orElse(null);

        User user = User.builder()
                .fullNames(request.getFullNames())
                .email(email)
                .phoneNumber(request.getPhoneNumber())
                .password(userService.encodePassword(request.getPassword()))
                .role(ERole.ROLE_CUSTOMER)
                .customer(customer)
                .status(EStatus.ACTIVE)
                .mustChangePassword(false)
                .emailVerified(true)
                .build();
        return userRepository.save(user);
    }

    public UserProfileResponse getCurrentProfile() {
        User user = securityService.getCurrentUser();
        return toProfile(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(ProfileUpdateRequest request) {
        User user = securityService.getCurrentUser();
        user.setFullNames(request.getFullNames());
        user.setPhoneNumber(request.getPhoneNumber());
        return toProfile(userRepository.save(user));
    }

    @Transactional
    public UserProfileResponse changePassword(ChangePasswordRequest request) {
        User user = securityService.getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from the current password.");
        }

        user.setPassword(userService.encodePassword(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        if (authAccountEmailService.isEmailAvailable()) {
            authAccountEmailService.sendPasswordChanged(user.getEmail(), user.getFullNames());
        }

        return toProfile(user);
    }

    private void validatePasswordResetEligible(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("No account found for this email."));
        if (user.getStatus() == EStatus.INACTIVE) {
            throw new BadRequestException("Account is inactive. Contact administrator.");
        }
    }

    private OtpPendingResponse toPendingResponse(OtpDispatchResult dispatch) {
        return OtpPendingResponse.builder()
                .otpRequired(true)
                .email(dispatch.getEmail())
                .purpose(dispatch.getPurpose())
                .otpExpiresInSeconds(dispatch.getOtpExpiresInSeconds())
                .resendAvailableInSeconds(dispatch.getResendAvailableInSeconds())
                .message(dispatch.getMessage())
                .build();
    }

    private UserProfileResponse toProfile(User user) {
        return UserProfileResponse.builder()
                .userId(user.getId())
                .fullNames(user.getFullNames())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .customerId(user.getCustomer() != null ? user.getCustomer().getId() : null)
                .mustChangePassword(Boolean.TRUE.equals(user.getMustChangePassword()))
                .emailVerified(Boolean.TRUE.equals(user.getEmailVerified()))
                .build();
    }
}
