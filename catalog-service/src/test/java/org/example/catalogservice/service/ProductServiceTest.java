package org.example.catalogservice.service;

import org.example.catalogservice.dto.CreateProductRequest;
import org.example.catalogservice.exception.ProductNotFoundException;
import org.example.catalogservice.model.Product;
import org.example.catalogservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void create_savesProduct() {
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(1L);
            return product;
        });

        var response = productService.create(new CreateProductRequest("Book", new BigDecimal("12.50")));

        assertEquals(1L, response.id());
        assertEquals("Book", response.name());
        assertEquals(new BigDecimal("12.50"), response.price());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void getById_missing_throws() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> productService.getById(99L));
    }
}
