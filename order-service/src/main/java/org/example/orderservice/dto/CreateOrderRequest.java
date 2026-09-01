package org.example.orderservice.dto;

import java.util.List;

public record CreateOrderRequest(List<OrderItemRequest> items) {
}
