package org.example.orderservice.service;

import org.example.orderservice.dto.CreateOrderRequest;
import org.example.orderservice.dto.OrderItemRequest;
import org.example.orderservice.kafka.OrderEventPublisher;
import org.example.orderservice.model.Order;
import org.example.orderservice.model.OrderStatus;
import org.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderService orderService;

    @Test
    void create_savesNewOrderAndPublishesCreated() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        orderService.create(new CreateOrderRequest(List.of(new OrderItemRequest(7L, 2))));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertEquals(OrderStatus.NEW, captor.getValue().getStatus());
        assertEquals(1, captor.getValue().getItems().size());
        verify(orderEventPublisher).publishOrderCreated(captor.getValue());
    }

    @Test
    void create_emptyItems_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.create(new CreateOrderRequest(List.of())));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void confirm_fromNew_publishesConfirmed() {
        Order order = newOrder(3L, OrderStatus.NEW);
        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));

        orderService.confirm(3L);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verify(orderEventPublisher).publishOrderConfirmed(3L);
    }

    @Test
    void confirm_alreadyConfirmed_isIdempotent() {
        Order order = newOrder(3L, OrderStatus.CONFIRMED);
        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));

        orderService.confirm(3L);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verify(orderEventPublisher, never()).publishOrderConfirmed(any());
    }

    @Test
    void markFailed_fromNew_cancelsAndPublishes() {
        Order order = newOrder(4L, OrderStatus.NEW);
        when(orderRepository.findById(4L)).thenReturn(Optional.of(order));

        orderService.markFailed(4L);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderEventPublisher).publishOrderCancelled(4L);
    }

    private static Order newOrder(Long id, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }
}
