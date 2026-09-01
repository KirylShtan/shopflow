package org.example.inventoryservice.exception;

public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException(Long productId) {
        super("Stock not found for productId: " + productId);
    }
}
