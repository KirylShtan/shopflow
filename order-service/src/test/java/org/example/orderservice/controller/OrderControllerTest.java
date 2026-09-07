package org.example.orderservice.controller;

import org.example.orderservice.dto.OrderItemResponse;
import org.example.orderservice.dto.OrderResponse;
import org.example.orderservice.model.OrderStatus;
import org.example.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void create_returnsOrder() throws Exception {
        OrderResponse response = new OrderResponse(
                1L,
                OrderStatus.NEW,
                LocalDateTime.of(2026, 9, 7, 12, 0),
                List.of(new OrderItemResponse(10L, 7L, 2)));
        when(orderService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"productId":7,"quantity":2}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderStatus").value("NEW"))
                .andExpect(jsonPath("$.items[0].productId").value(7));
    }

    @Test
    void getById_returnsOrder() throws Exception {
        OrderResponse response = new OrderResponse(
                1L,
                OrderStatus.CONFIRMED,
                LocalDateTime.of(2026, 9, 7, 12, 0),
                List.of());
        when(orderService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("CONFIRMED"));
    }
}
