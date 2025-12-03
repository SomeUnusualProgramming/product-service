package com.example.productservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String category;
    private Double price;
    private Integer stockQuantity;
    private Long originalProductId;

    // Pola do historii zmian
    private String eventType; // CREATED, UPDATED, DELETED, LOW_STOCK
    private LocalDateTime eventTime;

    // Konstruktor pomocniczy do testów
    public Product(String name, Double price) {
        this.name = name;
        this.price = price;
        this.eventTime = LocalDateTime.now();
    }
}
