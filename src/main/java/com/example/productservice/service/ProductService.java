package com.example.productservice.service;

import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.kafka.ProductProducer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductProducer productProducer;

    public ProductService(ProductRepository productRepository,
                          ProductProducer productProducer) {
        this.productRepository = productRepository;
        this.productProducer = productProducer;
    }

    // Tylko aktualne produkty (bez historii)
    public List<Product> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .filter(p -> p.getEventType() == null || p.getEventType().equals("CREATED"))
                .toList();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    // Historia produktu
    public List<Product> getProductHistory(Long id) {
        List<Product> history = productRepository.findByOriginalProductIdOrderByEventTimeDesc(id);
        System.out.println("GET history for product id=" + id + ": found " + history.size() + " events");
        return history;
    }

    public Product createProduct(Product product) {
        product.setEventType("CREATED");
        product.setEventTime(LocalDateTime.now());

        Product saved = productRepository.save(product);
        productProducer.sendMessage(saved); // wysyłamy event do Kafka
        return saved;
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id).map(existing -> {
            // Tworzymy wpis historii
            Product history = new Product();
            history.setName(existing.getName());
            history.setDescription(existing.getDescription());
            history.setCategory(existing.getCategory());
            history.setPrice(existing.getPrice());
            history.setStockQuantity(existing.getStockQuantity());
            history.setOriginalProductId(existing.getId());
            history.setEventType("UPDATED");
            history.setEventTime(LocalDateTime.now());
            productRepository.save(history);

            // Aktualizacja produktu
            existing.setName(updatedProduct.getName());
            existing.setDescription(updatedProduct.getDescription());
            existing.setCategory(updatedProduct.getCategory());
            existing.setPrice(updatedProduct.getPrice());
            existing.setStockQuantity(updatedProduct.getStockQuantity());
            Product saved = productRepository.save(existing);

            productProducer.sendMessage(saved); // event do Kafka
            return saved;
        }).orElse(null);
    }

    public void deleteProduct(Long id) {
        productRepository.findById(id).ifPresent(product -> {
            // Historia usunięcia
            Product history = new Product();
            history.setName(product.getName());
            history.setDescription(product.getDescription());
            history.setCategory(product.getCategory());
            history.setPrice(product.getPrice());
            history.setStockQuantity(product.getStockQuantity());
            history.setOriginalProductId(product.getId());
            history.setEventType("DELETED");
            history.setEventTime(LocalDateTime.now());
            productRepository.save(history);

            productRepository.deleteById(id);
            productProducer.sendMessage(history); // event do Kafka
        });
    }
}

