package com.example.productservice.constant;

public final class AppConstants {

    private AppConstants() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static final class Event {
        public static final String TYPE_CREATED = "CREATED";
        public static final String TYPE_UPDATED = "UPDATED";
        public static final String TYPE_DELETED = "DELETED";

        private Event() {
            throw new AssertionError("Cannot instantiate utility class");
        }
    }

    public static final class Kafka {
        public static final String TOPIC_PRODUCTS = "products-topic";
        public static final String TOPIC_PRODUCTS_LEGACY = "products";
        public static final String GROUP_ID_COMBINED = "product-combined-group";
        public static final String GROUP_ID_DEFAULT = "product-group";

        private Kafka() {
            throw new AssertionError("Cannot instantiate utility class");
        }
    }

    public static final class API {
        public static final String BASE_PATH = "/api/products";
        public static final String PATH_BY_ID = "/{id:[0-9]+}";
        public static final String PATH_HISTORY = "/{id:[0-9]+}/history";

        private API() {
            throw new AssertionError("Cannot instantiate utility class");
        }
    }

    public static final class ErrorCode {
        public static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
        public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
        public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
        public static final String UNAUTHORIZED = "UNAUTHORIZED";
        public static final String TENANT_MISSING = "TENANT_MISSING";
        public static final String METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
        public static final String ENDPOINT_NOT_FOUND = "ENDPOINT_NOT_FOUND";
        public static final String INVALID_ARGUMENT = "INVALID_ARGUMENT";
        public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

        private ErrorCode() {
            throw new AssertionError("Cannot instantiate utility class");
        }
    }

    public static final class Validation {
        public static final String PRODUCT_NAME_REQUIRED = "Product name is required";
        public static final String PRODUCT_DESCRIPTION_REQUIRED = "Product description is required";
        public static final String PRODUCT_CATEGORY_REQUIRED = "Product category is required";
        public static final String PRODUCT_PRICE_REQUIRED = "Product price is required";
        public static final String PRODUCT_PRICE_POSITIVE = "Price must be greater than 0";
        public static final String STOCK_QUANTITY_REQUIRED = "Stock quantity is required";
        public static final String STOCK_QUANTITY_NON_NEGATIVE = "Stock quantity cannot be negative";

        private Validation() {
            throw new AssertionError("Cannot instantiate utility class");
        }
    }

    public static final class Logger {
        public static final String HISTORY_RETRIEVED = "Retrieved history for product id={}: found {} events";
        public static final String HANDLING_CREATED = "Handling CREATED event for product id={}";
        public static final String HANDLING_UPDATED = "Handling UPDATED event for product id={}";
        public static final String HANDLING_DELETED = "Handling DELETED event for product id={}";
        public static final String MESSAGE_SENT = "Message sent to topic '{}': {}";
        public static final String MESSAGE_SEND_FAILED = "Failed to send message to topic '{}': {}";
        public static final String HISTORY_SAVED = "History saved: id={}";
        public static final String ERROR_KAFKA_DESERIALIZE = "Error deserializing Kafka message from topic '{}'";
        public static final String ERROR_PROCESSING_KAFKA = "Error processing Kafka message";
        public static final String ERROR_SERIALIZING = "Error serializing product to JSON";
        public static final String KAFKA_MESSAGE_RECEIVED = "Kafka message received from topic '{}': {}";
        public static final String UNKNOWN_EVENT_TYPE = "Unknown event type: {}";

        private Logger() {
            throw new AssertionError("Cannot instantiate utility class");
        }
    }

    public static final class Search {
        public static final String PARAM_SEARCH = "search";
        public static final String PARAM_CATEGORY = "category";
        public static final String PARAM_MIN_PRICE = "minPrice";
        public static final String PARAM_MAX_PRICE = "maxPrice";
        public static final String PARAM_MIN_STOCK = "minStock";
        public static final String PARAM_MAX_STOCK = "maxStock";
        public static final String PARAM_SORT_BY = "sortBy";
        public static final String PARAM_SORT_ORDER = "order";
        public static final String PARAM_PAGE = "page";
        public static final String PARAM_SIZE = "size";

        public static final String SORT_BY_NAME = "name";
        public static final String SORT_BY_PRICE = "price";
        public static final String SORT_BY_STOCK = "stockQuantity";
        public static final String SORT_BY_CREATED = "eventTime";

        public static final String SORT_ASC = "asc";
        public static final String SORT_DESC = "desc";

        public static final int DEFAULT_PAGE = 0;
        public static final int DEFAULT_SIZE = 20;
        public static final int MAX_SIZE = 100;

        private Search() {
            throw new AssertionError("Cannot instantiate utility class");
        }
    }
}
