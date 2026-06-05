package rw.gov.wasac.ubsystem.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import rw.gov.wasac.ubsystem.enums.EOtpPurpose;
import rw.gov.wasac.ubsystem.enums.ERole;
import rw.gov.wasac.ubsystem.message.EmailService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthAccountEmailService {

    private final ObjectProvider<EmailService> emailServiceProvider;

    public void sendOtp(String toEmail, String code, EOtpPurpose purpose, int validityMinutes, int resendCooldownSeconds) {
        EmailService emailService = requireEmailService();
        String subject = switch (purpose) {
            case LOGIN -> "Login Verification Code";
            case EMAIL_VERIFICATION -> "Email Verification Code";
            case PASSWORD_RESET -> "Password Reset Code";
        };
        String body = """
                Dear user,

                Your one-time verification code is: %s

                This code is valid for %d minutes.
                You may request a new code after %d seconds if it expires or you did not receive it.

                If you did not request this code, please ignore this email.

                Regards,
                Utility Billing System
                """.formatted(code, validityMinutes, resendCooldownSeconds);
        emailService.sendNotification(toEmail, subject, body);
    }

    public void sendWelcomeCredentials(String toEmail, String fullNames, String temporaryPassword, ERole role) {
        EmailService emailService = requireEmailService();
        String body = """
                Dear %s,

                Your %s account has been created on the Utility Billing System.

                Email: %s
                Temporary password: %s

                For security, you must change this password on your first login.
                After signing in with your password, you will receive a one-time code (OTP) by email to complete login.

                Regards,
                Utility Billing System
                """.formatted(fullNames, role.name(), toEmail, temporaryPassword);
        emailService.sendNotification(toEmail, "Your Account Credentials", body);
    }

    public void sendPasswordChanged(String toEmail, String fullNames) {
        EmailService emailService = requireEmailService();
        String body = """
                Dear %s,

                Your account password was changed successfully.
                If you did not make this change, contact your administrator immediately.

                Regards,
                Utility Billing System
                """.formatted(fullNames);
        emailService.sendNotification(toEmail, "Password Changed", body);
    }

    private EmailService requireEmailService() {
        EmailService emailService = emailServiceProvider.getIfAvailable();
        if (emailService == null || !emailService.isConfigured()) {
            log.warn("Email is not configured — message was not sent");
            throw new IllegalStateException("Email service is not configured. Set spring.mail.username in application.properties.");
        }
        return emailService;
    }

    public boolean isEmailAvailable() {
        EmailService emailService = emailServiceProvider.getIfAvailable();
        return emailService != null && emailService.isConfigured();
    }
}
