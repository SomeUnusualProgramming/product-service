package com.example.productservice.mapper;

import com.example.productservice.dto.PageResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public class PageMapper {

    private PageMapper() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static <T> PageResponseDTO<T> toPageResponseDTO(Page<T> page) {
        return PageResponseDTO.<T>builder()
            .content(page.getContent())
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .isFirst(page.isFirst())
            .isLast(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
}
