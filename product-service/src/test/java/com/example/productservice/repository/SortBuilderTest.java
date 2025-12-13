package com.example.productservice.repository;

import com.example.productservice.constant.AppConstants;
import com.example.productservice.dto.ProductFilterDTO;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;

class SortBuilderTest {

    @Test
    void testBuildSortWithDefaultValues() {
        ProductFilterDTO filter = ProductFilterDTO.builder().build();

        Sort result = SortBuilder.buildSort(filter);

        assertNotNull(result);
        Sort.Order order = result.getOrderFor(AppConstants.Search.SORT_BY_NAME);
        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
        assertEquals(AppConstants.Search.SORT_BY_NAME, order.getProperty());
    }

    @Test
    void testBuildSortWithCustomSortBy() {
        ProductFilterDTO filter = ProductFilterDTO.builder()
                .sortBy(AppConstants.Search.SORT_BY_PRICE)
                .build();

        Sort result = SortBuilder.buildSort(filter);

        assertNotNull(result);
        Sort.Order order = result.getOrderFor(AppConstants.Search.SORT_BY_PRICE);
        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
        assertEquals(AppConstants.Search.SORT_BY_PRICE, order.getProperty());
    }

    @Test
    void testBuildSortWithDescendingOrder() {
        ProductFilterDTO filter = ProductFilterDTO.builder()
                .sortOrder(AppConstants.Search.SORT_DESC)
                .build();

        Sort result = SortBuilder.buildSort(filter);

        assertNotNull(result);
        Sort.Order order = result.getOrderFor(AppConstants.Search.SORT_BY_NAME);
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void testBuildSortWithCustomFieldAndDescendingOrder() {
        ProductFilterDTO filter = ProductFilterDTO.builder()
                .sortBy(AppConstants.Search.SORT_BY_STOCK)
                .sortOrder(AppConstants.Search.SORT_DESC)
                .build();

        Sort result = SortBuilder.buildSort(filter);

        assertNotNull(result);
        Sort.Order order = result.getOrderFor(AppConstants.Search.SORT_BY_STOCK);
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
        assertEquals(AppConstants.Search.SORT_BY_STOCK, order.getProperty());
    }

    @Test
    void testBuildSortWithCaseInsensitiveSortOrder() {
        ProductFilterDTO filter = ProductFilterDTO.builder()
                .sortOrder("DESC")
                .build();

        Sort result = SortBuilder.buildSort(filter);

        assertNotNull(result);
        Sort.Order order = result.getOrderFor(AppConstants.Search.SORT_BY_NAME);
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void testBuildSortWithNullSortBy() {
        ProductFilterDTO filter = ProductFilterDTO.builder()
                .sortBy(null)
                .sortOrder(AppConstants.Search.SORT_ASC)
                .build();

        Sort result = SortBuilder.buildSort(filter);

        assertNotNull(result);
        Sort.Order order = result.getOrderFor(AppConstants.Search.SORT_BY_NAME);
        assertNotNull(order);
        assertEquals(AppConstants.Search.SORT_BY_NAME, order.getProperty());
    }

    @Test
    void testBuildSortWithNullSortOrder() {
        ProductFilterDTO filter = ProductFilterDTO.builder()
                .sortBy(AppConstants.Search.SORT_BY_PRICE)
                .sortOrder(null)
                .build();

        Sort result = SortBuilder.buildSort(filter);

        assertNotNull(result);
        Sort.Order order = result.getOrderFor(AppConstants.Search.SORT_BY_PRICE);
        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    void testBuildSortWithEventTimeField() {
        ProductFilterDTO filter = ProductFilterDTO.builder()
                .sortBy(AppConstants.Search.SORT_BY_CREATED)
                .sortOrder(AppConstants.Search.SORT_DESC)
                .build();

        Sort result = SortBuilder.buildSort(filter);

        assertNotNull(result);
        Sort.Order order = result.getOrderFor(AppConstants.Search.SORT_BY_CREATED);
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
        assertEquals(AppConstants.Search.SORT_BY_CREATED, order.getProperty());
    }
}
