package com.example.productservice.service;

import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    // konstruktor do wstrzykiwania zależności
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // pobiera wszystkie produkty
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // tworzy nowy produkt
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    // pobiera produkt po ID
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    // usuwa produkt po ID
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // aktualizuje istniejący produkt
    public Product updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setName(updatedProduct.getName());
                    product.setPrice(updatedProduct.getPrice());
                    return productRepository.save(product);
                })
                .orElse(null);
    }
}
