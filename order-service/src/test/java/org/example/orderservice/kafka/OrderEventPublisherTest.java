package org.example.orderservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.orderservice.event.OrderCreatedEvent;
import org.example.orderservice.model.Order;
import org.example.orderservice.model.OrderItem;
import org.example.orderservice.model.OutBoxMessage;
import org.example.orderservice.repository.OutBoxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private OutBoxRepository outBoxRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OrderEventPublisher publisher;

    @Test
    void publishOrderCreated_writesOutboxAndDoesNotSendKafka() {
        Order order = new Order();
        order.setId(42L);
        OrderItem item = new OrderItem();
        item.setProductId(7L);
        item.setQuantity(2);
        order.addItem(item);

        publisher.publishOrderCreated(order);

        ArgumentCaptor<OutBoxMessage> captor = ArgumentCaptor.forClass(OutBoxMessage.class);
        verify(outBoxRepository).save(captor.capture());
        OutBoxMessage message = captor.getValue();
        assertEquals(KafkaTopics.ORDER_CREATED, message.getTopic());
        assertEquals("42", message.getKey());
        assertFalse(message.isSent());
        assertTrue(message.getPayload().contains("\"productId\":7"));
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void publishOrderConfirmed_sendsKafkaDirectly() {
        publisher.publishOrderConfirmed(9L);
        verify(kafkaTemplate).send(KafkaTopics.ORDER_CONFIRMED, "9",
                new org.example.orderservice.event.OrderConfirmedEvent(9L));
    }
}
