package com.example.productservice.controller;

import com.example.productservice.constant.AppConstants;
import com.example.productservice.dto.PageResponseDTO;
import com.example.productservice.dto.ProductFilterDTO;
import com.example.productservice.dto.ProductRequestDTO;
import com.example.productservice.dto.ProductResponseDTO;
import com.example.productservice.mapper.PageMapper;
import com.example.productservice.mapper.ProductMapper;
import com.example.productservice.model.Product;
import com.example.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API.BASE_PATH)
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> products = productService.getAllProducts()
            .stream()
            .map(productMapper::productToProductResponseDTO)
            .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping(AppConstants.API.PATH_BY_ID)
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        var product = productService.getProductById(id);
        return ResponseEntity.ok(productMapper.productToProductResponseDTO(product));
    }

    @GetMapping(AppConstants.API.PATH_HISTORY)
    public ResponseEntity<List<ProductResponseDTO>> getProductHistory(@PathVariable Long id) {
        List<ProductResponseDTO> history = productService.getProductHistory(id)
            .stream()
            .map(productMapper::productToProductResponseDTO)
            .toList();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponseDTO<ProductResponseDTO>> searchProducts(
            @Valid ProductFilterDTO filter) {
        Page<Product> products = productService.searchAndFilterProducts(filter);
        PageResponseDTO<ProductResponseDTO> response = PageMapper.toPageResponseDTO(
            products.map(productMapper::productToProductResponseDTO)
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO productRequestDTO) {
        var product = productMapper.productRequestDTOToProduct(productRequestDTO);
        var createdProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(productMapper.productToProductResponseDTO(createdProduct));
    }

    @PutMapping(AppConstants.API.PATH_BY_ID)
    public ResponseEntity<ProductResponseDTO> updateProduct(
        @PathVariable Long id,
        @Valid @RequestBody ProductRequestDTO productRequestDTO) {
        var updatedProduct = productService.updateProduct(id, productRequestDTO);
        return ResponseEntity.ok(productMapper.productToProductResponseDTO(updatedProduct));
    }

    @DeleteMapping(AppConstants.API.PATH_BY_ID)
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
