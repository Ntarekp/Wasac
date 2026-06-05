package rw.gov.wasac.ubsystem.otp;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.wasac.ubsystem.auth.AuthAccountEmailService;
import rw.gov.wasac.ubsystem.enums.EOtpPurpose;
import rw.gov.wasac.ubsystem.exception.BadRequestException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthAccountEmailService authAccountEmailService;

    @Value("${app.otp.validity-minutes:10}")
    private int validityMinutes;

    @Value("${app.otp.resend-cooldown-seconds:60}")
    private int resendCooldownSeconds;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public OtpDispatchResult issueOtp(String email, EOtpPurpose purpose) {
        String normalizedEmail = normalizeEmail(email);
        Otp existing = otpRepository
                .findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(normalizedEmail, purpose)
                .orElse(null);

        if (existing != null && existing.getExpiresAt().isAfter(LocalDateTime.now())) {
            long secondsSinceSent = Duration.between(existing.getLastSentAt(), LocalDateTime.now()).getSeconds();
            if (secondsSinceSent < resendCooldownSeconds) {
                long waitSeconds = resendCooldownSeconds - secondsSinceSent;
                return OtpDispatchResult.builder()
                        .email(normalizedEmail)
                        .purpose(purpose)
                        .newlyIssued(false)
                        .otpExpiresInSeconds(secondsUntil(existing.getExpiresAt()))
                        .resendAvailableInSeconds(waitSeconds)
                        .message("OTP already sent. You can request a new code in " + waitSeconds + " seconds.")
                        .build();
            }
            return refreshOtp(existing);
        }

        return createOtp(normalizedEmail, purpose);
    }

    @Transactional
    public OtpDispatchResult resendOtp(String email, EOtpPurpose purpose) {
        String normalizedEmail = normalizeEmail(email);
        Otp existing = otpRepository
                .findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(normalizedEmail, purpose)
                .orElseThrow(() -> new BadRequestException(
                        "No active OTP session found. Start the process again (login, register, or password reset)."));

        if (existing.getExpiresAt().isBefore(LocalDateTime.now())) {
            return createOtp(normalizedEmail, purpose);
        }

        long secondsSinceSent = Duration.between(existing.getLastSentAt(), LocalDateTime.now()).getSeconds();
        if (secondsSinceSent < resendCooldownSeconds) {
            long waitSeconds = resendCooldownSeconds - secondsSinceSent;
            throw new BadRequestException(
                    "Please wait " + waitSeconds + " seconds before requesting a new OTP.");
        }

        return refreshOtp(existing);
    }

    @Transactional
    public void verifyOtp(String email, String code, EOtpPurpose purpose) {
        String normalizedEmail = normalizeEmail(email);
        Otp otp = otpRepository
                .findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(normalizedEmail, purpose)
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP. Request a new code."));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired. Request a new code.");
        }

        if (!passwordEncoder.matches(code, otp.getCodeHash())) {
            throw new BadRequestException("Invalid OTP code.");
        }

        otp.setUsed(true);
        otpRepository.save(otp);
    }

    public int getValidityMinutes() {
        return validityMinutes;
    }

    public int getResendCooldownSeconds() {
        return resendCooldownSeconds;
    }

    private OtpDispatchResult createOtp(String email, EOtpPurpose purpose) {
        String code = generateCode();
        LocalDateTime now = LocalDateTime.now();
        Otp otp = Otp.builder()
                .email(email)
                .codeHash(passwordEncoder.encode(code))
                .purpose(purpose)
                .expiresAt(now.plusMinutes(validityMinutes))
                .used(false)
                .createdAt(now)
                .lastSentAt(now)
                .build();
        otpRepository.save(otp);
        authAccountEmailService.sendOtp(email, code, purpose, validityMinutes, resendCooldownSeconds);

        return OtpDispatchResult.builder()
                .email(email)
                .purpose(purpose)
                .newlyIssued(true)
                .otpExpiresInSeconds(validityMinutes * 60L)
                .resendAvailableInSeconds(resendCooldownSeconds)
                .message("OTP sent to " + email + ". It is valid for " + validityMinutes + " minutes.")
                .build();
    }

    private OtpDispatchResult refreshOtp(Otp otp) {
        String code = generateCode();
        LocalDateTime now = LocalDateTime.now();
        otp.setCodeHash(passwordEncoder.encode(code));
        otp.setExpiresAt(now.plusMinutes(validityMinutes));
        otp.setLastSentAt(now);
        otpRepository.save(otp);
        authAccountEmailService.sendOtp(otp.getEmail(), code, otp.getPurpose(), validityMinutes, resendCooldownSeconds);

        return OtpDispatchResult.builder()
                .email(otp.getEmail())
                .purpose(otp.getPurpose())
                .newlyIssued(true)
                .otpExpiresInSeconds(validityMinutes * 60L)
                .resendAvailableInSeconds(resendCooldownSeconds)
                .message("A new OTP was sent to " + otp.getEmail() + ". It is valid for " + validityMinutes + " minutes.")
                .build();
    }

    private String generateCode() {
        int value = secureRandom.nextInt(1_000_000);
        return String.format("%06d", value);
    }

    private long secondsUntil(LocalDateTime expiresAt) {
        return Math.max(0, Duration.between(LocalDateTime.now(), expiresAt).getSeconds());
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required.");
        }
        return email.trim().toLowerCase();
    }
}
