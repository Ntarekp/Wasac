package rw.gov.wasac.ubsystem.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "spring.mail.username")
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.institution.name:Utility Billing System}")
    private String institutionName;

    public boolean isConfigured() {
        return fromAddress != null && !fromAddress.isBlank();
    }

    public void sendNotification(String toEmail, String subject, String body) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Skipping email dispatch — customer has no email address");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(institutionName + " — " + subject);
        message.setText(body);
        mailSender.send(message);
        log.info("Email sent to {}", toEmail);
    }
}
