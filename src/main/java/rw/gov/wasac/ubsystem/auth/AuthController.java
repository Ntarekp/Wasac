package rw.gov.wasac.ubsystem.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.gov.wasac.ubsystem.user.User;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Step 1 — validate password and send login OTP by email")
    public ResponseEntity<OtpPendingResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Step 2 — verify OTP and receive JWT (purpose=LOGIN)")
    public ResponseEntity<LoginResponse> verifyOtp(@Valid @RequestBody OtpVerifyDTO request) {
        return ResponseEntity.ok(authService.verifyOtpAndLogin(request));
    }

    @PostMapping("/otp/request")
    @Operation(summary = "Request OTP for signup (EMAIL_VERIFICATION) or password reset")
    public ResponseEntity<OtpPendingResponse> requestOtp(@Valid @RequestBody OtpRequestDTO request) {
        return ResponseEntity.ok(authService.requestOtp(request));
    }

    @PostMapping("/otp/resend")
    @Operation(summary = "Resend OTP after cooldown (60s default)")
    public ResponseEntity<OtpPendingResponse> resendOtp(@Valid @RequestBody OtpRequestDTO request) {
        return ResponseEntity.ok(authService.resendOtp(request));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Step 2 — reset password using PASSWORD_RESET OTP (after /api/auth/otp/request)")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Public self-registration (ROLE_CUSTOMER, requires EMAIL_VERIFICATION OTP)")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserProfileResponse> getProfile() {
        return ResponseEntity.ok(authService.getCurrentProfile());
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current user profile (name and phone)")
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(authService.updateProfile(request));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password (required on first login when mustChangePassword=true)")
    public ResponseEntity<UserProfileResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(authService.changePassword(request));
    }
}
