package com.example.productservice.dto;

import com.example.productservice.constant.AppConstants;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDTO {

    @NotBlank(message = AppConstants.Validation.PRODUCT_NAME_REQUIRED)
    @Size(min = 1, max = 255, message = "Product name must be between 1 and 255 characters")
    private String name;

    @NotBlank(message = AppConstants.Validation.PRODUCT_DESCRIPTION_REQUIRED)
    @Size(min = 1, max = 2000, message = "Product description must be between 1 and 2000 characters")
    private String description;

    @NotBlank(message = AppConstants.Validation.PRODUCT_CATEGORY_REQUIRED)
    @Size(min = 1, max = 100, message = "Product category must be between 1 and 100 characters")
    private String category;

    @NotNull(message = AppConstants.Validation.PRODUCT_PRICE_REQUIRED)
    @DecimalMin(value = "0.0", inclusive = false, message = AppConstants.Validation.PRODUCT_PRICE_POSITIVE)
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 fractional digits")
    private Double price;

    @NotNull(message = AppConstants.Validation.STOCK_QUANTITY_REQUIRED)
    @PositiveOrZero(message = AppConstants.Validation.STOCK_QUANTITY_NON_NEGATIVE)
    private Integer stockQuantity;
}
