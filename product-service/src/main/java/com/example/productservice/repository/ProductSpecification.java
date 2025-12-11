package com.example.productservice.repository;

import com.example.productservice.constant.AppConstants;
import com.example.productservice.dto.ProductFilterDTO;
import com.example.productservice.model.Product;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    private ProductSpecification() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static Specification<Product> buildSearchSpecification(ProductFilterDTO filter, String tenantId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));

            predicates.add(criteriaBuilder.or(
                criteriaBuilder.isNull(root.get("eventType")),
                criteriaBuilder.equal(root.get("eventType"), AppConstants.Event.TYPE_CREATED)
            ));

            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern)
                ));
            }

            if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("category"), filter.getCategory()));
            }

            if (filter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            if (filter.getMinStock() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("stockQuantity"), filter.getMinStock()));
            }

            if (filter.getMaxStock() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("stockQuantity"), filter.getMaxStock()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
