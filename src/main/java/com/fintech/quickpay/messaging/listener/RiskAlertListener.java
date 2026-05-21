package com.fintech.quickpay.messaging.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.quickpay.entity.User;
import com.fintech.quickpay.messaging.event.RiskAlertEvent;
import com.fintech.quickpay.repository.UserRepository;
import com.fintech.quickpay.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RiskAlertListener {

    private static final Logger log = LoggerFactory.getLogger(RiskAlertListener.class);

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public RiskAlertListener(ObjectMapper objectMapper, NotificationService notificationService,
                             UserRepository userRepository) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @KafkaListener(topics = "fintech.risk-events")
    public void onRiskAlert(String message) {
        try {
            RiskAlertEvent event = objectMapper.readValue(message, RiskAlertEvent.class);

            Optional<User> userOpt = userRepository.findByCustomerNo(event.getCustomerNo());
            if (userOpt.isEmpty()) {
                log.warn("RiskAlertListener: no user found for customerNo={}, skipping notification",
                        event.getCustomerNo());
                return;
            }

            User user = userOpt.get();
            String title = String.format("Risk Alert [%s] — Level: %s",
                    event.getType(), event.getRiskLevel());
            String content = String.format(
                    "A risk event has been detected on your account. " +
                    "Type: %s, Risk Level: %s, Score: %d. %s",
                    event.getType(), event.getRiskLevel(), event.getScore(), event.getDescription());

            notificationService.sendSecurityAlert(user, title, content);

            log.info("RiskAlertListener: security alert sent to user={} (customerNo={}) for riskLevel={}, score={}",
                    user.getUsername(), event.getCustomerNo(), event.getRiskLevel(), event.getScore());

        } catch (Exception e) {
            log.error("RiskAlertListener: failed to process message: {}", e.getMessage(), e);
        }
    }
}
