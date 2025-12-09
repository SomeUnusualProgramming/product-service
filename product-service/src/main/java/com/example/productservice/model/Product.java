package com.example.productservice.model;

import com.example.productservice.constant.AppConstants;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = AppConstants.Validation.PRODUCT_NAME_REQUIRED)
    private String name;

    @NotBlank(message = AppConstants.Validation.PRODUCT_DESCRIPTION_REQUIRED)
    private String description;

    @NotBlank(message = AppConstants.Validation.PRODUCT_CATEGORY_REQUIRED)
    private String category;

    @NotNull(message = AppConstants.Validation.PRODUCT_PRICE_REQUIRED)
    @DecimalMin(value = "0.0", inclusive = false, message = AppConstants.Validation.PRODUCT_PRICE_POSITIVE)
    private Double price;

    @NotNull(message = AppConstants.Validation.STOCK_QUANTITY_REQUIRED)
    @PositiveOrZero(message = AppConstants.Validation.STOCK_QUANTITY_NON_NEGATIVE)
    private Integer stockQuantity;

    private Long originalProductId;
    private String eventType;
    private LocalDateTime eventTime;

    public Product(String name, Double price) {
        this.name = name;
        this.price = price;
        this.eventTime = LocalDateTime.now();
    }
}
