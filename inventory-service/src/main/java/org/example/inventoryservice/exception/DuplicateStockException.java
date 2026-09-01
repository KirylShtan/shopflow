package org.example.inventoryservice.exception;

public class DuplicateStockException extends RuntimeException {
    public DuplicateStockException(Long productId) {
        super("Stock already exists for productId: " + productId);
    }
}
