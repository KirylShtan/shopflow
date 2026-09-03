package org.example.orderservice.event;

public record InventoryFailedEvent(Long orderId, String reason) {
}
