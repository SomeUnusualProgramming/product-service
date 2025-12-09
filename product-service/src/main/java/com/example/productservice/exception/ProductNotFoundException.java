package com.example.productservice.exception;

public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException(Long productId) {
        super("Product", "id", productId);
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
