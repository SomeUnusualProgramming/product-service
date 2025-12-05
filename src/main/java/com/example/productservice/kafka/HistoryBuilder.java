package com.example.productservice.kafka;

import com.example.productservice.model.Product;

import java.time.LocalDateTime;

public class HistoryBuilder {

    private HistoryBuilder() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static Product createHistory(Product productEvent) {
        Product history = new Product();
        history.setName(productEvent.getName());
        history.setDescription(productEvent.getDescription());
        history.setCategory(productEvent.getCategory());
        history.setPrice(productEvent.getPrice());
        history.setStockQuantity(productEvent.getStockQuantity());
        history.setEventType(productEvent.getEventType());
        history.setEventTime(productEvent.getEventTime());
        history.setOriginalProductId(productEvent.getId());
        return history;
    }

    public static Product createHistory(Product productEvent, String eventType) {
        Product history = createHistory(productEvent);
        history.setEventType(eventType);
        history.setEventTime(LocalDateTime.now());
        return history;
    }
}
