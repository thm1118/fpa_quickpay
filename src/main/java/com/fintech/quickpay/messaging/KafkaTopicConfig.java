package com.fintech.quickpay.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic quickpayNotificationTopic() {
        return TopicBuilder.name("quickpay.notification").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name("fintech.payment-events").partitions(1).replicas(1).build();
    }
}
