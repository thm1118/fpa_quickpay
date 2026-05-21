package com.fintech.quickpay.messaging.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.quickpay.entity.Transaction;
import com.fintech.quickpay.entity.User;
import com.fintech.quickpay.messaging.EventPublisher;
import com.fintech.quickpay.messaging.event.NotificationEvent;
import com.fintech.quickpay.messaging.event.PaymentEvent;
import com.fintech.quickpay.repository.TransactionRepository;
import com.fintech.quickpay.repository.UserRepository;
import com.fintech.quickpay.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public NotificationListener(ObjectMapper objectMapper,
                                NotificationService notificationService,
                                TransactionRepository transactionRepository,
                                UserRepository userRepository,
                                EventPublisher eventPublisher) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(topics = "quickpay.notification")
    public void onNotification(String message) {
        try {
            NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);

            // (a) Create Notification records via NotificationService
            Transaction transaction = transactionRepository.findByTransactionNo(event.getTransactionNo())
                    .orElse(null);
            if (transaction == null) {
                log.warn("NotificationListener: transaction not found for transactionNo={}", event.getTransactionNo());
                return;
            }

            // Notify payer (fromUser)
            if (event.getFromUserId() != null) {
                userRepository.findById(event.getFromUserId()).ifPresent(fromUser ->
                        notificationService.sendTransactionNotification(fromUser, transaction, false)
                );
            }

            // Notify receiver (toUser) if present
            if (event.getToUserId() != null) {
                userRepository.findById(event.getToUserId()).ifPresent(toUser ->
                        notificationService.sendTransactionNotification(toUser, transaction, true)
                );
            }

            // (b) Publish PaymentEvent to fintech.payment-events
            String fromAccountNo = transaction.getFromAccount() != null
                    ? transaction.getFromAccount().getAccountNo() : null;
            String toAccountNo = transaction.getToAccount() != null
                    ? transaction.getToAccount().getAccountNo() : null;

            // Resolve payer username from fromAccount.user, fall back to toAccount.user for recharge
            String username = null;
            if (transaction.getFromAccount() != null && transaction.getFromAccount().getUser() != null) {
                username = transaction.getFromAccount().getUser().getUsername();
            } else if (transaction.getToAccount() != null && transaction.getToAccount().getUser() != null) {
                username = transaction.getToAccount().getUser().getUsername();
            }

            PaymentEvent paymentEvent = new PaymentEvent(
                    event.getType(),
                    event.getTransactionNo(),
                    username,
                    fromAccountNo,
                    toAccountNo,
                    event.getAmount()
            );

            eventPublisher.publish("fintech.payment-events", event.getTransactionNo(), paymentEvent);

        } catch (Exception e) {
            log.error("NotificationListener: failed to process message: {}", e.getMessage(), e);
        }
    }
}
