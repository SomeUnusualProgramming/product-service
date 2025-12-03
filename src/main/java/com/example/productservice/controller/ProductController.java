package com.example.productservice.controller;

import com.example.productservice.model.Product;
import com.example.productservice.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET /api/products - tylko aktualne produkty
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // GET /api/products/test
    @GetMapping("/test")
    public String test() {
        return "Controller OK";
    }

    // GET /api/products/ping
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    // GET /api/products/{id} tylko dla liczb
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }

    // Historia produktu
    @GetMapping("/{id:[0-9]+}/history")
    public List<Product> getProductHistory(@PathVariable Long id) {
        return productService.getProductHistory(id);
    }

    // Tworzenie produktu
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    // Aktualizacja produktu
    @PutMapping("/{id:[0-9]+}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product updatedProduct) {
        Product product = productService.updateProduct(id, updatedProduct);
        return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }

    // Usunięcie produktu
    @DeleteMapping("/{id:[0-9]+}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
