package org.example.catalogservice.dto;

import java.math.BigDecimal;

public record ProductResponse(Long id, String name, BigDecimal price) {
}
