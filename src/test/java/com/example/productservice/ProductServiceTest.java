package com.example.productservice;

import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
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

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllProducts() {
        List<Product> mockProducts = Arrays.asList(new Product("Apple", 1.0), new Product("Banana", 2.0));
        when(productRepository.findAll()).thenReturn(mockProducts);

        List<Product> products = productService.getAllProducts();
        assertEquals(2, products.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testCreateProduct() {
        Product productToCreate = new Product("Orange", 3.0);
        Product savedProduct = new Product("Orange", 3.0);
        savedProduct.setId(1L);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        Product result = productService.createProduct(productToCreate);
        System.out.println("DEBUG: result = " + result);

        assertEquals("Orange", result.getName());
        assertEquals(3.0, result.getPrice());
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetProductById() {
        Product product = new Product("Apple", 1.0);
        product.setId(1L);

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);
        System.out.println("DEBUG: result = " + result);

        assertEquals("Apple", result.getName());
        assertEquals(1.0, result.getPrice());
        assertEquals(1L, result.getId());
    }

    @Test
    void testDeleteProduct() {
        doNothing().when(productRepository).deleteById(1L);
        productService.deleteProduct(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }
}
