package org.example.notificationservice.kafka;

import org.example.notificationservice.event.OrderCancelledEvent;
import org.example.notificationservice.event.OrderConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderNotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderNotificationListener.class);

    @KafkaListener(
            topics = KafkaTopics.ORDER_CONFIRMED,
            properties = "spring.json.value.default.type=org.example.notificationservice.event.OrderConfirmedEvent"

    )
    public void onConfirmed(OrderConfirmedEvent event){
        logger.info("EMAIL: Order {} confirmed. Thank you for your purhase", event.orderId());
    }

    @KafkaListener(
            topics = KafkaTopics.ORDER_CANCELLED,
            properties = "spring.json.value.default.type=org.example.notificationservice.event.OrderCancelledEvent"
    )
    public void onCancelled(OrderCancelledEvent event){
        logger.info("EMAIL: Order {} cancelled. Stock was not available", event.orderId());
    }

}
