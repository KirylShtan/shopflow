package org.example.orderservice.kafka;

import org.example.orderservice.event.OrderCreatedEvent;
import org.example.orderservice.event.OrderItemEvent;
import org.example.orderservice.model.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(Order order){
        List<OrderItemEvent> items = order.getItems().stream()
                .map(i -> new OrderItemEvent(i.getProductId(),i.getQuantity()))
                .toList();
        OrderCreatedEvent event = new OrderCreatedEvent(order.getId(), items);

        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, order.getId().toString(),event );

    }

}
