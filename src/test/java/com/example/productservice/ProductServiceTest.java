package com.example.productservice.service;

import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository repository;
    private ProductService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(ProductRepository.class);
        service = new ProductService(repository);
    }

    @Test
    void testGetAllProducts() {
        Product p1 = new Product("Test1", "Desc1", "Cat1", 10.0, 5);
        Product p2 = new Product("Test2", "Desc2", "Cat2", 20.0, 3);
        when(repository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<Product> products = service.getAllProducts();
        assertEquals(2, products.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void testCreateProduct() {
        Product p = new Product("Test", "Desc", "Cat", 15.0, 7);
        when(repository.save(p)).thenReturn(p);

        Product created = service.createProduct(p);
        assertEquals("Test", created.getName());
        verify(repository, times(1)).save(p);
    }

    @Test
    void testGetProductByIdNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertNull(service.getProductById(1L));
    }
}
