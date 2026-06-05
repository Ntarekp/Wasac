package rw.gov.wasac.ubsystem.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import rw.gov.wasac.ubsystem.customer.Customer;
import rw.gov.wasac.ubsystem.customer.CustomerService;
import rw.gov.wasac.ubsystem.enums.EOtpPurpose;
import rw.gov.wasac.ubsystem.enums.ERole;
import rw.gov.wasac.ubsystem.enums.EStatus;
import rw.gov.wasac.ubsystem.otp.OtpDispatchResult;
import rw.gov.wasac.ubsystem.otp.OtpService;
import rw.gov.wasac.ubsystem.security.CustomUserDetailsService;
import rw.gov.wasac.ubsystem.security.JwtUtil;
import rw.gov.wasac.ubsystem.security.SecurityService;
import rw.gov.wasac.ubsystem.user.User;
import rw.gov.wasac.ubsystem.user.UserRepository;
import rw.gov.wasac.ubsystem.user.UserService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private UserService userService;
    @Mock private CustomerService customerService;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private JwtUtil jwtUtil;
    @Mock private OtpService otpService;
    @Mock private SecurityService securityService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthAccountEmailService authAccountEmailService;

    @InjectMocks private AuthService authService;

    @Test
    void register_verifiesEmailOtpAndCreatesRoleCustomerAccount() {
        RegisterRequest request = new RegisterRequest();
        request.setFullNames("Melissa Urujeni");
        request.setEmail("ukmeuk1@gmail.com");
        request.setPhoneNumber("+250795068718");
        request.setPassword("YmguSkngWK65");
        request.setOtp("123456");

        Customer customer = Customer.builder().id(UUID.randomUUID()).email("ukmeuk1@gmail.com").build();
        when(userRepository.existsByEmail("ukmeuk1@gmail.com")).thenReturn(false);
        when(customerService.findByEmail("ukmeuk1@gmail.com")).thenReturn(Optional.of(customer));
        when(userService.encodePassword("YmguSkngWK65")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = authService.register(request);

        verify(otpService).verifyOtp("ukmeuk1@gmail.com", "123456", EOtpPurpose.EMAIL_VERIFICATION);
        assertNotNull(created);
        assertEquals(ERole.ROLE_CUSTOMER, created.getRole());
        assertEquals(EStatus.ACTIVE, created.getStatus());
        assertEquals(Boolean.TRUE, created.getEmailVerified());
        assertEquals(Boolean.FALSE, created.getMustChangePassword());
        assertEquals(customer, created.getCustomer());
    }

    @Test
    void login_authenticatesAndIssuesLoginOtp() {
        LoginRequest request = new LoginRequest();
        request.setEmail("ukmeuk1@gmail.com");
        request.setPassword("YmguSkngWK65");

        User user = User.builder()
                .email("ukmeuk1@gmail.com")
                .status(EStatus.ACTIVE)
                .build();

        OtpDispatchResult dispatch = OtpDispatchResult.builder()
                .email("ukmeuk1@gmail.com")
                .purpose(EOtpPurpose.LOGIN)
                .otpExpiresInSeconds(600)
                .resendAvailableInSeconds(60)
                .message("OTP sent")
                .build();

        when(userRepository.findByEmail("ukmeuk1@gmail.com")).thenReturn(Optional.of(user));
        when(otpService.issueOtp("ukmeuk1@gmail.com", EOtpPurpose.LOGIN)).thenReturn(dispatch);

        OtpPendingResponse response = authService.login(request);

        verify(authenticationManager).authenticate(any());
        verify(otpService).issueOtp("ukmeuk1@gmail.com", EOtpPurpose.LOGIN);
        verify(authAccountEmailService, never()).sendWelcomeCredentials(any(), any(), any(), any());
        assertEquals("ukmeuk1@gmail.com", response.getEmail());
        assertEquals(EOtpPurpose.LOGIN, response.getPurpose());
        assertEquals(600, response.getOtpExpiresInSeconds());
        assertEquals(60, response.getResendAvailableInSeconds());
    }
}
