package org.example.orderservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.orderservice.event.OrderCreatedEvent;
import org.example.orderservice.event.OrderItemEvent;
import org.example.orderservice.model.OutBoxMessage;
import org.example.orderservice.repository.OutBoxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutBoxPublisherTest {

    @Mock
    private OutBoxRepository outBoxRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OutBoxPublisher publisher;

    @Test
    void publishPending_sendsAndMarksSent() throws Exception {
        OrderCreatedEvent event = new OrderCreatedEvent(5L, List.of(new OrderItemEvent(1L, 2)));
        OutBoxMessage message = new OutBoxMessage(
                KafkaTopics.ORDER_CREATED,
                "5",
                objectMapper.writeValueAsString(event));
        when(outBoxRepository.findBySentFalseOrderByIdAsc()).thenReturn(List.of(message));

        publisher.publishPending();

        verify(kafkaTemplate).send(eq(KafkaTopics.ORDER_CREATED), eq("5"), any(OrderCreatedEvent.class));
        assertTrue(message.isSent());
    }

    @Test
    void publishPending_onKafkaError_keepsUnsent() throws Exception {
        OrderCreatedEvent event = new OrderCreatedEvent(5L, List.of(new OrderItemEvent(1L, 2)));
        OutBoxMessage message = new OutBoxMessage(
                KafkaTopics.ORDER_CREATED,
                "5",
                objectMapper.writeValueAsString(event));
        when(outBoxRepository.findBySentFalseOrderByIdAsc()).thenReturn(List.of(message));
        when(kafkaTemplate.send(any(), any(), any())).thenThrow(new RuntimeException("broker down"));

        publisher.publishPending();

        assertFalse(message.isSent());
    }

    @Test
    void publishPending_empty_doesNothing() {
        when(outBoxRepository.findBySentFalseOrderByIdAsc()).thenReturn(List.of());

        publisher.publishPending();

        verify(kafkaTemplate, never()).send(any(), any(), any());
    }
}
