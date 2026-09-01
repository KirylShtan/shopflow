package org.example.orderservice.dto;

import org.example.orderservice.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(Long id, OrderStatus orderStatus,
                            LocalDateTime createdAt, List<OrderItemResponse> items) {
}
