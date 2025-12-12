package com.example.productservice.repository;

import com.example.productservice.constant.AppConstants;
import com.example.productservice.dto.ProductFilterDTO;
import org.springframework.data.domain.Sort;

/**
 * Utility class for building Spring Data Sort objects from filter criteria.
 * Provides factory methods to construct Sort specifications with default values
 * when sorting parameters are not specified.
 */
public class SortBuilder {

    private SortBuilder() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Builds a Spring Data Sort object based on filter criteria.
     * If sort field or direction are not specified in the filter, defaults to sorting by name
     * in ascending order.
     *
     * @param filter the product filter containing sort criteria
     * @return a configured Sort object for use in Spring Data queries
     */
    public static Sort buildSort(final ProductFilterDTO filter) {
        String sortBy = filter.getSortBy() != null
                ? filter.getSortBy()
                : AppConstants.Search.SORT_BY_NAME;
        String sortOrder = filter.getSortOrder() != null
                ? filter.getSortOrder()
                : AppConstants.Search.SORT_ASC;

        Sort.Direction direction = AppConstants.Search.SORT_DESC.equalsIgnoreCase(sortOrder)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(direction, sortBy);
    }
}
