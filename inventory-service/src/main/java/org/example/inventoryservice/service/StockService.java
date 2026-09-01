package org.example.inventoryservice.service;

import org.example.inventoryservice.dto.CreateStockRequest;
import org.example.inventoryservice.dto.StockResponse;
import org.example.inventoryservice.dto.UpdateStockRequest;
import org.example.inventoryservice.exception.DuplicateStockException;
import org.example.inventoryservice.exception.StockNotFoundException;
import org.example.inventoryservice.model.Stock;
import org.example.inventoryservice.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
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

    private StockResponse toResponse(Stock stock){
        return new StockResponse(stock.getId(), stock.getProductId(), stock.getQuantity());
    }
}
