package rw.gov.wasac.ubsystem.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.wasac.ubsystem.customer.Customer;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageDispatchScheduler {

    private final MessageRepository messageRepository;
    private final ObjectProvider<EmailService> emailServiceProvider;

    @Scheduled(fixedDelayString = "${app.message.dispatch-interval-ms:60000}")
    @Transactional
    public void dispatchPendingMessages() {
        EmailService emailService = emailServiceProvider.getIfAvailable();
        if (emailService == null || !emailService.isConfigured()) {
            return;
        }

        List<Message> pending = messageRepository.findBySentFalse();
        for (Message message : pending) {
            try {
                Customer customer = message.getCustomer();
                String subject = notificationSubject(message.getMessageType());
                emailService.sendNotification(customer.getEmail(), subject, message.getContent());
                message.setSent(true);
                messageRepository.save(message);
            } catch (Exception ex) {
                log.error("Failed to dispatch message {}: {}", message.getId(), ex.getMessage());
            }
        }
    }

    private static String notificationSubject(String messageType) {
        if ("BILL_GENERATED".equals(messageType)) {
            return "Bill Generated";
        }
        if ("PAYMENT_COMPLETE".equals(messageType)) {
            return "Payment Complete";
        }
        return "Notification";
    }
}
