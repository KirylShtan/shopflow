package org.example.inventoryservice.dto;

public record CreateStockRequest(Long productId,Integer quantity) {
}
