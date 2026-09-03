package org.example.orderservice.event;

import java.util.List;

public record OrderCreatedEvent(Long orderId, List<OrderItemEvent> items) {
}
