package org.example.inventoryservice.event;

public record OrderItemEvent(Long productId, Integer quantity) {
}
