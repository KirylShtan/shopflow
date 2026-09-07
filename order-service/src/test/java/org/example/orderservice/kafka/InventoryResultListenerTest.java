package org.example.orderservice.kafka;

import org.example.orderservice.event.InventoryFailedEvent;
import org.example.orderservice.event.InventoryReservedEvent;
import org.example.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryResultListenerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private InventoryResultListener listener;

    @Test
    void onReserved_confirmsOrder() {
        listener.onReserved(new InventoryReservedEvent(9L));
        verify(orderService).confirm(9L);
    }

    @Test
    void onFailed_marksFailed() {
        listener.onFailed(new InventoryFailedEvent(9L, "out of stock"));
        verify(orderService).markFailed(9L);
    }
}
