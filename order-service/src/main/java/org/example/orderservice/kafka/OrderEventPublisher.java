package org.example.orderservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.orderservice.event.OrderCancelledEvent;
import org.example.orderservice.event.OrderConfirmedEvent;
import org.example.orderservice.event.OrderCreatedEvent;
import org.example.orderservice.event.OrderItemEvent;
import org.example.orderservice.model.Order;
import org.example.orderservice.model.OutBoxMessage;
import org.example.orderservice.repository.OutBoxRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OutBoxRepository outBoxRepository;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                               OutBoxRepository outBoxRepository,
                               ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.outBoxRepository = outBoxRepository;

    }

    public void publishOrderCreated(Order order){
        List<OrderItemEvent> items = order.getItems().stream()
                .map(i -> new OrderItemEvent(i.getProductId(), i.getQuantity()))
                .toList();
        OrderCreatedEvent event = new OrderCreatedEvent(order.getId(), items);
        try{
            String payload = objectMapper.writeValueAsString(event);
            outBoxRepository.save(new OutBoxMessage(KafkaTopics.ORDER_CREATED
                    ,order.getId().toString()
                    , payload));
        }catch (JsonProcessingException e){
            throw new IllegalStateException("Failed to serialize order event", e);
        }
    }



    public void publishOrderConfirmed(Long orderId){
        kafkaTemplate.send(KafkaTopics.ORDER_CONFIRMED, orderId.toString(),
                new OrderConfirmedEvent(orderId));
    }

    public void publishOrderCancelled(Long orderId){
        kafkaTemplate.send(
                KafkaTopics.ORDER_CANCELLED,
                orderId.toString(),
                new OrderCancelledEvent(orderId)
        );
    }

}
