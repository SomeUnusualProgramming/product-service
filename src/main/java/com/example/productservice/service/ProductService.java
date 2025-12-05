package com.example.productservice.service;

import com.example.productservice.constant.AppConstants;
import com.example.productservice.exception.ProductNotFoundException;
import com.example.productservice.kafka.HistoryBuilder;
import com.example.productservice.kafka.ProductProducer;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.security.TenantProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final ProductProducer productProducer;

    public ProductService(ProductRepository productRepository,
                          ProductProducer productProducer) {
        this.productRepository = productRepository;
        this.productProducer = productProducer;
    }

    public List<Product> getAllProducts() {
        String tenantId = TenantProvider.getCurrentTenantId();
        return productRepository.findCurrentProductsByTenant(tenantId);
    }

    public Product getProductById(Long id) {
        String tenantId = TenantProvider.getCurrentTenantId();
        return productRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));
    }

    public List<Product> getProductHistory(Long id) {
        String tenantId = TenantProvider.getCurrentTenantId();
        List<Product> history = productRepository.findByOriginalProductIdAndTenantIdOrderByEventTimeDesc(id, tenantId);
        logger.info(AppConstants.Logger.HISTORY_RETRIEVED, id, history.size());
        return history;
    }

    public Product createProduct(Product product) {
        product.setEventType(AppConstants.Event.TYPE_CREATED);
        product.setEventTime(LocalDateTime.now());

        Product saved = productRepository.save(product);
        productProducer.sendMessage(saved);
        return saved;
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        String tenantId = TenantProvider.getCurrentTenantId();
        return productRepository.findByIdAndTenantId(id, tenantId).map(existing -> {
            Product history = HistoryBuilder.createHistory(existing, AppConstants.Event.TYPE_UPDATED);
            productRepository.save(history);

            existing.setName(updatedProduct.getName());
            existing.setDescription(updatedProduct.getDescription());
            existing.setCategory(updatedProduct.getCategory());
            existing.setPrice(updatedProduct.getPrice());
            existing.setStockQuantity(updatedProduct.getStockQuantity());
            Product saved = productRepository.save(existing);

            productProducer.sendMessage(saved);
            return saved;
        }).orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));
    }

    public void deleteProduct(Long id) {
        String tenantId = TenantProvider.getCurrentTenantId();
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));
        
        Product history = HistoryBuilder.createHistory(product, AppConstants.Event.TYPE_DELETED);
        productRepository.save(history);

        productRepository.deleteById(id);
        productProducer.sendMessage(history);
    }

    public void processProductEvent(Product productEvent) {
        String tenantId = productEvent.getTenantId();
        if (tenantId == null) {
            tenantId = TenantProvider.getTenantIdOrNull();
        }
        
        switch (productEvent.getEventType()) {
            case AppConstants.Event.TYPE_CREATED -> {
                logger.info(AppConstants.Logger.HANDLING_CREATED, productEvent.getId());
                productRepository.save(productEvent);
            }
            case AppConstants.Event.TYPE_UPDATED -> {
                logger.info(AppConstants.Logger.HANDLING_UPDATED, productEvent.getId());
                if (tenantId != null) {
                    productRepository.findByIdAndTenantId(productEvent.getId(), tenantId).ifPresent(existing -> {
                        existing.setName(productEvent.getName());
                        existing.setDescription(productEvent.getDescription());
                        existing.setCategory(productEvent.getCategory());
                        existing.setPrice(productEvent.getPrice());
                        existing.setStockQuantity(productEvent.getStockQuantity());
                        productRepository.save(existing);
                    });
                }
            }
            case AppConstants.Event.TYPE_DELETED -> {
                logger.info(AppConstants.Logger.HANDLING_DELETED, productEvent.getId());
                productRepository.deleteById(productEvent.getId());
            }
            default -> logger.warn(AppConstants.Logger.UNKNOWN_EVENT_TYPE, productEvent.getEventType());
        }
    }

    public void handleProductEventFromKafka(Product productEvent, boolean processEvent) {
        try {
            Objects.requireNonNull(productEvent, "Product event cannot be null");

            productEvent.setEventTime(LocalDateTime.now());

            Product history = HistoryBuilder.createHistory(productEvent);
            productRepository.save(history);
            logger.info(AppConstants.Logger.HISTORY_SAVED, history.getId());

            if (processEvent) {
                processProductEvent(productEvent);
            }
        } catch (Exception e) {
            logger.error(AppConstants.Logger.ERROR_PROCESSING_KAFKA, e);
        }
    }
}

