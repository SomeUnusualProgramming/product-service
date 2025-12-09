package com.example.productservice.mapper;

import com.example.productservice.dto.ProductRequestDTO;
import com.example.productservice.dto.ProductResponseDTO;
import com.example.productservice.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    ProductResponseDTO productToProductResponseDTO(Product product);

    Product productRequestDTOToProduct(ProductRequestDTO productRequestDTO);

    void updateProductFromDTO(ProductRequestDTO dto, @MappingTarget Product product);
}
