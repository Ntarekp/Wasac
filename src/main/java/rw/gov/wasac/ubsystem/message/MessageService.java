package rw.gov.wasac.ubsystem.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rw.gov.wasac.ubsystem.customer.Customer;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;

    /**
     * Called on bill generation OR payment complete.
     * Format: Dear <Name>, Your <Month/Year> utility bill of <amount>FRW has been successfully processed
     */
    public Message sendBillNotification(Customer customer, String monthYear, double amount, String type) {
        String content = String.format(
                "Dear %s, Your %s utility bill of %.2fFRW has been successfully processed",
                customer.getFullNames(), monthYear, amount
        );

        Message message = Message.builder()
                .customer(customer)
                .content(content)
                .messageType(type)
                .sent(true) // In production, would be false until dispatched via SMS/email gateway
                .build();

        Message saved = messageRepository.save(message);
        log.info("Notification [{}] -> {}: {}", type, customer.getEmail(), content);
        return saved;
    }

    public List<Message> getMessagesByCustomer(UUID customerId) {
        return messageRepository.findByCustomerId(customerId);
    }

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }
}