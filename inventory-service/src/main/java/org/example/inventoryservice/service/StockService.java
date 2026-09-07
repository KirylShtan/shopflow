package org.example.inventoryservice.service;

import org.example.inventoryservice.dto.CreateStockRequest;
import org.example.inventoryservice.dto.StockResponse;
import org.example.inventoryservice.dto.UpdateStockRequest;
import org.example.inventoryservice.event.OrderItemEvent;
import org.example.inventoryservice.exception.DuplicateStockException;
import org.example.inventoryservice.exception.StockNotFoundException;
import org.example.inventoryservice.model.ProcessedOrder;
import org.example.inventoryservice.model.Stock;
import org.example.inventoryservice.repository.ProcessedOrderRepository;
import org.example.inventoryservice.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final ProcessedOrderRepository processedOrderRepository;

    public StockService(StockRepository stockRepository,
                        ProcessedOrderRepository processedOrderRepository) {
        this.stockRepository = stockRepository;
        this.processedOrderRepository = processedOrderRepository;
    }

    @Transactional(readOnly = true)
    public List<StockResponse> findAll() {
        return stockRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StockResponse getByProductId(Long productId) {
        Stock stock = stockRepository.findByProductId(productId).orElseThrow(
                () -> new StockNotFoundException(productId));
        return toResponse(stock);
    }

    @Transactional
    public StockResponse create(CreateStockRequest request) {
        if(stockRepository.existsByProductId(request.productId())) {
            throw new DuplicateStockException(request.productId());
        }
        Stock stock = new Stock(request.productId(), request.quantity());
        return toResponse(stockRepository.save(stock));
    }

    @Transactional
    public StockResponse update(Long productId, UpdateStockRequest request) {
        Stock stock = stockRepository.findByProductId(productId).orElseThrow(
                () -> new StockNotFoundException(productId));
        stock.setQuantity(request.quantity());
        return toResponse(stock);
    }

    @Transactional
    public void delete(Long productId) {
        Stock stock = stockRepository.findByProductId(productId).orElseThrow(
                () -> new StockNotFoundException(productId));
        stockRepository.delete(stock);
    }

    @Transactional
    public void reserve(Long productId, Integer quantity) {
        Stock stock = stockRepository.findByProductId(productId).orElseThrow(
                () -> new StockNotFoundException(productId));
        if (stock.getQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Not enough stock for productId: " + productId
                    + ", need= " + quantity
                    + ", have-=" + stock.getQuantity());

        }
        stock.setQuantity(stock.getQuantity() - quantity);
    }

    @Transactional
    public void reserveAll(List<OrderItemEvent> items) {
        for (OrderItemEvent item : items) {
            reserve(item.productId(), item.quantity());
        }
    }

    @Transactional
    public boolean reserveForOrder(Long orderId, List<OrderItemEvent> items) {
        if (processedOrderRepository.existsById(orderId)) {
            return false;
        }
        reserveAll(items);
        processedOrderRepository.save(new ProcessedOrder(orderId));
        return true;
    }

    private StockResponse toResponse(Stock stock){
        return new StockResponse(stock.getId(), stock.getProductId(), stock.getQuantity());
    }
}
