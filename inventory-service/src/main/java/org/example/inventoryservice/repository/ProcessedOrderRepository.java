package org.example.inventoryservice.repository;

import org.example.inventoryservice.model.ProcessedOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedOrderRepository extends JpaRepository<ProcessedOrder, Long> {
}
