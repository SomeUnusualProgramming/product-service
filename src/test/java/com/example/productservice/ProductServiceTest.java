package com.example.productservice;

import com.example.productservice.exception.ProductNotFoundException;
import com.example.productservice.kafka.ProductProducer;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.security.TenantContext;
import com.example.productservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductProducer productProducer;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        TenantContext.setTenantId("test-tenant");
    }

    @Test
    void testGetAllProducts() {
        List<Product> mockProducts = Arrays.asList(
                new Product("Apple", 1.0),
                new Product("Banana", 2.0)
        );
        when(productRepository.findCurrentProductsByTenant("test-tenant")).thenReturn(mockProducts);

        List<Product> products = productService.getAllProducts();

        assertEquals(2, products.size());
        verify(productRepository, times(1)).findCurrentProductsByTenant("test-tenant");
    }

    @Test
    void testCreateProduct() {
        Product productToCreate = new Product("Orange", 3.0);
        Product savedProduct = new Product("Orange", 3.0);
        savedProduct.setId(1L);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        Product result = productService.createProduct(productToCreate);

        assertEquals("Orange", result.getName());
        assertEquals(3.0, result.getPrice());
        assertEquals(1L, result.getId());

        verify(productProducer, times(1)).sendMessage(any(Product.class));
    }

    @Test
    void testGetProductById() {
        Product product = new Product("Apple", 1.0);
        product.setId(1L);

        when(productRepository.findByIdAndTenantId(1L, "test-tenant")).thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        assertEquals("Apple", result.getName());
        assertEquals(1.0, result.getPrice());
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetProductByIdThrowsExceptionWhenNotFound() {
        when(productRepository.findByIdAndTenantId(999L, "test-tenant")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(999L));
    }

    @Test
    void testDeleteProduct() {
        Product existingProduct = new Product("Apple", 1.0);
        existingProduct.setId(1L);

        when(productRepository.findByIdAndTenantId(1L, "test-tenant")).thenReturn(Optional.of(existingProduct));

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).save(any(Product.class));
        verify(productRepository, times(1)).deleteById(1L);
        verify(productProducer, times(1)).sendMessage(any(Product.class));
    }

    @Test
    void testUpdateProductThrowsExceptionWhenNotFound() {
        Product updatedProduct = new Product("Updated", 5.0);

        when(productRepository.findByIdAndTenantId(999L, "test-tenant")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(999L, updatedProduct));
    }

    @Test
    void testDeleteProductThrowsExceptionWhenNotFound() {
        when(productRepository.findByIdAndTenantId(999L, "test-tenant")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(999L));
    }
}
