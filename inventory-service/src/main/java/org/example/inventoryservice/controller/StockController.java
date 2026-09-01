package org.example.inventoryservice.controller;

import org.example.inventoryservice.dto.CreateStockRequest;
import org.example.inventoryservice.dto.StockResponse;
import org.example.inventoryservice.dto.UpdateStockRequest;
import org.example.inventoryservice.service.StockService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }
    @GetMapping
    public List<StockResponse> getAllStocks() {
        return stockService.findAll();
    }

    @GetMapping("/{productId}")
    public StockResponse getByProductId(@PathVariable Long productId) {
        return stockService.getByProductId(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockResponse create(@RequestBody CreateStockRequest request) {
        return stockService.create(request);
    }

    @PutMapping("/{productId}")
    public StockResponse update(@PathVariable Long productId,
                                @RequestBody UpdateStockRequest request) {
        return stockService.update(productId, request);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long productId) {
        stockService.delete(productId);
    }
}
