package org.example.orderservice.kafka;

import org.example.orderservice.event.InventoryFailedEvent;
import org.example.orderservice.event.InventoryReservedEvent;
import org.example.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryResultListener {

    private static final Logger logger = LoggerFactory.getLogger(InventoryResultListener.class);

    private final OrderService orderService;

    public InventoryResultListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_RESERVED,
            properties = "spring.json.value.default.type=org.example.orderservice.event.InventoryReservedEvent")
    public void onReserved(InventoryReservedEvent event) {
        logger.info("Received InventoryReserved: orderId={}", event.orderId());
        orderService.confirm(event.orderId());
        logger.info("Order confirmed: orderId={}", event.orderId());
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_FAILED,
            properties = "spring.json.value.default.type=org.example.orderservice.event.InventoryFailedEvent")
    public void onFailed(InventoryFailedEvent event) {
        logger.warn("Received InventoryFailed: orderId={}, reason={}", event.orderId(), event.reason());
        orderService.markFailed(event.orderId());
        logger.info("Order cancelled: orderId={}", event.orderId());
    }
}
