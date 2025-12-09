package com.example.productservice.repository;

import com.example.productservice.constant.AppConstants;
import com.example.productservice.dto.ProductFilterDTO;
import org.springframework.data.domain.Sort;

public class SortBuilder {

    private SortBuilder() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static Sort buildSort(ProductFilterDTO filter) {
        String sortBy = filter.getSortBy() != null ? filter.getSortBy() : AppConstants.Search.SORT_BY_NAME;
        String sortOrder = filter.getSortOrder() != null ? filter.getSortOrder() : AppConstants.Search.SORT_ASC;

        Sort.Direction direction = AppConstants.Search.SORT_DESC.equalsIgnoreCase(sortOrder) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;

        return Sort.by(direction, sortBy);
    }
}
