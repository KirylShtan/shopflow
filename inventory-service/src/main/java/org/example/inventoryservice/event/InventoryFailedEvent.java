package org.example.inventoryservice.event;

public record InventoryFailedEvent(Long orderId, String reason) {
}
