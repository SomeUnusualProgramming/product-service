package com.example.productservice.service;

import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    public Product getProductById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Product createProduct(Product product) {
        return repository.save(product);
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        return repository.findById(id)
                .map(product -> {
                    product.setName(updatedProduct.getName());
                    product.setDescription(updatedProduct.getDescription());
                    product.setCategory(updatedProduct.getCategory());
                    product.setPrice(updatedProduct.getPrice());
                    product.setStockQuantity(updatedProduct.getStockQuantity());
                    return repository.save(product);
                })
                .orElse(null);
    }

    public void deleteProduct(Long id) {
        repository.deleteById(id);
    }

    public List<Product> getProductsByCategory(String category) {
        return repository.findByCategory(category);
    }
}
