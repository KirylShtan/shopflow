package org.example.inventoryservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_orders")
@Getter
@Setter
@NoArgsConstructor
public class ProcessedOrder {

    @Id
    private Long orderId;

    private LocalDateTime processedAt;

    public ProcessedOrder(Long orderId){
        this.orderId = orderId;
        this.processedAt = LocalDateTime.now();
    }

}
