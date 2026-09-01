package org.example.inventoryservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock")
@Getter
@Setter
@NoArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    public Stock(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

}
