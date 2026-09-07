package org.example.inventoryservice.kafka;

import org.example.inventoryservice.event.InventoryFailedEvent;
import org.example.inventoryservice.event.InventoryReservedEvent;
import org.example.inventoryservice.event.OrderCreatedEvent;
import org.example.inventoryservice.event.OrderItemEvent;
import org.example.inventoryservice.service.StockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCreatedListenerTest {

    @Mock
    private StockService stockService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderCreatedListener listener;

    @Test
    void onOrderCreated_firstTime_sendsInventoryReservedOnce() {
        OrderCreatedEvent event = new OrderCreatedEvent(5L, List.of(new OrderItemEvent(1L, 1)));
        when(stockService.reserveForOrder(5L, event.items())).thenReturn(true);

        listener.onOrderCreated(event);

        verify(kafkaTemplate, times(1)).send(
                eq(KafkaTopics.INVENTORY_RESERVED),
                eq("5"),
                any(InventoryReservedEvent.class));
    }

    @Test
    void onOrderCreated_duplicate_sendsInventoryReservedOnceAndStops() {
        OrderCreatedEvent event = new OrderCreatedEvent(5L, List.of(new OrderItemEvent(1L, 1)));
        when(stockService.reserveForOrder(5L, event.items())).thenReturn(false);

        listener.onOrderCreated(event);

        verify(kafkaTemplate, times(1)).send(
                eq(KafkaTopics.INVENTORY_RESERVED),
                eq("5"),
                any(InventoryReservedEvent.class));
    }

    @Test
    void onOrderCreated_reserveFails_sendsInventoryFailed() {
        OrderCreatedEvent event = new OrderCreatedEvent(5L, List.of(new OrderItemEvent(1L, 1)));
        when(stockService.reserveForOrder(5L, event.items()))
                .thenThrow(new IllegalArgumentException("Not enough stock"));

        listener.onOrderCreated(event);

        verify(kafkaTemplate).send(
                eq(KafkaTopics.INVENTORY_FAILED),
                eq("5"),
                any(InventoryFailedEvent.class));
    }
}
