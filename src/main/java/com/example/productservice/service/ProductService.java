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

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> getProductHistory(Long id) {
        List<Product> history = productRepository.findByOriginalProductIdOrderByEventTimeDesc(id);
        System.out.println("GET history for product id=" + id + ": found " + history.size() + " events");
        return history;
    }

    public Product createProduct(Product product) {
        product.setEventType("CREATED");
        product.setEventTime(LocalDateTime.now());

        Product saved = productRepository.save(product);
        productProducer.sendMessage(saved); // wysyłamy cały obiekt Product, nie String

        return saved;
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id)
                .map(product -> {
                    Product history = new Product();
                    history.setName(product.getName());
                    history.setDescription(product.getDescription());
                    history.setCategory(product.getCategory());
                    history.setPrice(product.getPrice());
                    history.setStockQuantity(product.getStockQuantity());
                    history.setEventType("UPDATED");
                    history.setEventTime(LocalDateTime.now());
                    productRepository.save(history);

                    product.setName(updatedProduct.getName());
                    product.setPrice(updatedProduct.getPrice());
                    Product saved = productRepository.save(product);

                    productProducer.sendMessage(saved); // tu też wysyłamy obiekt

                    return saved;
                })
                .orElse(null);
    }

    public void deleteProduct(Long id) {
        productRepository.findById(id).ifPresent(product -> {
            Product history = new Product();
            history.setName(product.getName());
            history.setDescription(product.getDescription());
            history.setCategory(product.getCategory());
            history.setPrice(product.getPrice());
            history.setStockQuantity(product.getStockQuantity());
            history.setEventType("DELETED");
            history.setEventTime(LocalDateTime.now());
            productRepository.save(history);

            productRepository.deleteById(id);
            productProducer.sendMessage(history); // wysyłamy obiekt Product
        });
    }

}
