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
public class ProductFilterDTO {

    @Size(max = 255, message = "Search query must not exceed 255 characters")
    private String search;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum price must be greater than or equal to 0")
    private Double minPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Maximum price must be greater than or equal to 0")
    private Double maxPrice;

    @Min(value = 0, message = "Minimum stock must be greater than or equal to 0")
    private Integer minStock;

    @Min(value = 0, message = "Maximum stock must be greater than or equal to 0")
    private Integer maxStock;

    @Pattern(regexp = "^(name|price|stockQuantity|eventTime)$", 
             message = "Sort by must be one of: name, price, stockQuantity, eventTime")
    @Builder.Default
    private String sortBy = AppConstants.Search.SORT_BY_NAME;

    @Pattern(regexp = "^(asc|desc)$", message = "Sort order must be 'asc' or 'desc'")
    @Builder.Default
    private String sortOrder = AppConstants.Search.SORT_ASC;

    @Min(value = 0, message = "Page number must be greater than or equal to 0")
    @Builder.Default
    private Integer page = AppConstants.Search.DEFAULT_PAGE;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    @Builder.Default
    private Integer size = AppConstants.Search.DEFAULT_SIZE;
}
