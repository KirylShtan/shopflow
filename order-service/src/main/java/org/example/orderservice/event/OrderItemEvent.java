package org.example.orderservice.event;

public record OrderItemEvent(Long productId, Integer quantity) {
}
