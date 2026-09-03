package org.example.orderservice.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {
    @Bean
    DefaultErrorHandler errorHandler() {

        return new DefaultErrorHandler(new FixedBackOff(0L, 0));
    }
}
