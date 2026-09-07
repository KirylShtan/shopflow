package org.example.inventoryservice.kafka;

import org.example.inventoryservice.event.InventoryFailedEvent;
import org.example.inventoryservice.event.InventoryReservedEvent;
import org.example.inventoryservice.event.OrderCreatedEvent;
import org.example.inventoryservice.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderCreatedListener.class);

    private final StockService stockService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderCreatedListener(StockService stockService,
                                KafkaTemplate<String, Object> kafkaTemplate) {
        this.stockService = stockService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED)
    public void onOrderCreated(OrderCreatedEvent event) {
        logger.info("Received OrderCreated: orderId={}, items={}", event.orderId(), event.items());

        try{
            boolean reserved = stockService.reserveForOrder(event.orderId(), event.items());
            if(!reserved){
                logger.info("Duplicate OrderCreated, skip reserve. orderId={}", event.orderId());
                kafkaTemplate.send(KafkaTopics.INVENTORY_RESERVED,
                        event.orderId().toString(),
                        new InventoryReservedEvent(event.orderId()));
                return;
            }

            kafkaTemplate.send(
                    KafkaTopics.INVENTORY_RESERVED,
                    event.orderId().toString(),
                    new InventoryReservedEvent(event.orderId())
            );
            logger.info("Stock reserved, sent InventoryReserved: orderId={}", event.orderId());
        }catch(Exception e){
            logger.warn("Stock reserve failed for orderId={}: {}", event.orderId(), e.getMessage());
            kafkaTemplate.send(KafkaTopics.INVENTORY_FAILED,
                    event.orderId().toString(),
                    new InventoryFailedEvent(event.orderId(),e.getMessage()));
            logger.info("Sent InventoryFailed: orderId={}", event.orderId());
        }
    }
}
