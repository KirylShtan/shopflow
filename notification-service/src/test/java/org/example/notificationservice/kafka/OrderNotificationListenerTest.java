package org.example.notificationservice.kafka;

import org.example.notificationservice.event.OrderCancelledEvent;
import org.example.notificationservice.event.OrderConfirmedEvent;
import org.junit.jupiter.api.Test;

class OrderNotificationListenerTest {

    private final OrderNotificationListener listener = new OrderNotificationListener();

    @Test
    void onConfirmed_doesNotThrow() {
        listener.onConfirmed(new OrderConfirmedEvent(1L));
    }

    @Test
    void onCancelled_doesNotThrow() {
        listener.onCancelled(new OrderCancelledEvent(2L));
    }
}
