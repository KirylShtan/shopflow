package org.example.inventoryservice.service;

import org.example.inventoryservice.event.OrderItemEvent;
import org.example.inventoryservice.model.ProcessedOrder;
import org.example.inventoryservice.model.Stock;
import org.example.inventoryservice.repository.ProcessedOrderRepository;
import org.example.inventoryservice.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProcessedOrderRepository processedOrderRepository;

    @InjectMocks
    private StockService stockService;

    @Test
    void reserveForOrder_firstTime_reservesAndMarksProcessed() {
        Long orderId = 10L;
        Stock stock = new Stock(1L, 5);
        when(processedOrderRepository.existsById(orderId)).thenReturn(false);
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));

        boolean reserved = stockService.reserveForOrder(orderId, List.of(new OrderItemEvent(1L, 2)));

        assertTrue(reserved);
        assertEquals(3, stock.getQuantity());
        ArgumentCaptor<ProcessedOrder> captor = ArgumentCaptor.forClass(ProcessedOrder.class);
        verify(processedOrderRepository).save(captor.capture());
        assertEquals(orderId, captor.getValue().getOrderId());
    }

    @Test
    void reserveForOrder_duplicate_skipsReserve() {
        Long orderId = 10L;
        when(processedOrderRepository.existsById(orderId)).thenReturn(true);

        boolean reserved = stockService.reserveForOrder(orderId, List.of(new OrderItemEvent(1L, 2)));

        assertFalse(reserved);
        verify(stockRepository, never()).findByProductId(any());
        verify(processedOrderRepository, never()).save(any());
    }

    @Test
    void reserveForOrder_notEnoughStock_doesNotMarkProcessed() {
        Long orderId = 10L;
        Stock stock = new Stock(1L, 1);
        when(processedOrderRepository.existsById(orderId)).thenReturn(false);
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));

        assertThrows(IllegalArgumentException.class,
                () -> stockService.reserveForOrder(orderId, List.of(new OrderItemEvent(1L, 5))));

        verify(processedOrderRepository, never()).save(any());
    }
}
