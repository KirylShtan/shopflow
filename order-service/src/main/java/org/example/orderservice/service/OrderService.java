package org.example.orderservice.service;

import org.example.orderservice.dto.CreateOrderRequest;
import org.example.orderservice.dto.OrderItemRequest;
import org.example.orderservice.dto.OrderItemResponse;
import org.example.orderservice.dto.OrderResponse;
import org.example.orderservice.exception.OrderNotFoundException;
import org.example.orderservice.model.Order;
import org.example.orderservice.model.OrderItem;
import org.example.orderservice.model.OrderStatus;
import org.example.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new OrderNotFoundException(id));
        return toResponse(order);
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        if(request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        Order order = new Order();
        order.setStatus(OrderStatus.NEW);
        order.setCreatedAt(LocalDateTime.now());

        for (OrderItemRequest item : request.items()) {
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(item.productId());
            orderItem.setQuantity(item.quantity());
            order.addItem(orderItem);
        }
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancel(Long id){
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new OrderNotFoundException(id));
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException("Cannot cancel confirmed order");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(i.getId(), i.getProductId(), i.getQuantity()))
                .toList();
        return new OrderResponse(order.getId(),order.getStatus(),order.getCreatedAt(),items);
    }

}

