package com.example.productservice.constant;

/**
 * Centralized application constants.
 * Contains constants for events, Kafka topics, API paths, error codes, validation messages,
 * logging templates, and search parameters. This utility class is not instantiable.
 */
public final class AppConstants {

    private AppConstants() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Event type constants for product lifecycle events.
     */
    public static final class Event {

        /**
         * Constant representing a product creation event.
         */
        public static final String TYPE_CREATED = "CREATED";

        /**
         * Constant representing a product update event.
         */
        public static final String TYPE_UPDATED = "UPDATED";

        /**
         * Constant representing a product deletion event.
         */
        public static final String TYPE_DELETED = "DELETED";

        private Event() {
            throw new AssertionError("Cannot instantiate utility class");
        }
    }

    /**
     * Kafka configuration constants including topic names and consumer group IDs.
     */
    public static final class Kafka {

        /**
         * Kafka topic for product events (current version).
         */
        public static final String TOPIC_PRODUCTS = "products-topic";

        /**
         * Legacy Kafka topic for backward compatibility.
         */
        public static final String TOPIC_PRODUCTS_LEGACY = "products";

        /**
         * Consumer group ID for combined product event processing.
         */
        public static final String GROUP_ID_COMBINED = "product-combined-group";

        /**
         * Default consumer group ID for product events.
         */
        public static final String GROUP_ID_DEFAULT = "product-group";

        private Kafka() {
            throw new AssertionError("Cannot instantiate utility class");
        }
    }

    /**
     * API endpoint path constants.
     */
    public static final class API {

        /**
         * Base path for product API endpoints.
         */
        public static final String BASE_PATH = "/api/products";

        /**
         * API path pattern for retrieving a product by numeric ID.
         */
        public static final String PATH_BY_ID = "/{id:[0-9]+}";

        /**
         * API path pattern for retrieving product history by numeric ID.
         */
        public static final String PATH_HISTORY = "/{id:[0-9]+}/history";

        private API() {
            throw new AssertionError("Cannot instantiate utility class");
        }
    }

    /**
     * Error code constants for API error responses.
     */
    public static final class ErrorCode {

        /**
         * Error code for product not found.
         */
        public static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";

        /**
         * Error code for generic resource not found.
         */
        public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";

        /**
         * Error code for validation failures.
         */
        public static final String VALIDATION_ERROR = "VALIDATION_ERROR";

        /**
         * Error code for unauthorized access.
         */
        public static final String UNAUTHORIZED = "UNAUTHORIZED";

        /**
         * Error code for missing tenant context.
         */
        public static final String TENANT_MISSING = "TENANT_MISSING";

        /**
         * Error code for HTTP method not allowed.
         */
        public static final String METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";

        /**
         * Error code for endpoint not found.
         */
        public static final String ENDPOINT_NOT_FOUND = "ENDPOINT_NOT_FOUND";

        /**
         * Error code for invalid arguments.
         */
        public static final String INVALID_ARGUMENT = "INVALID_ARGUMENT";

        /**
         * Error code for internal server errors.
         */
        public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

        private ErrorCode() {
            throw new AssertionError("Cannot instantiate utility class");
        }
    }

    /**
     * Validation message constants.
     */
    public static final class Validation {

        /**
         * Validation message for missing product name.
         */
        public static final String PRODUCT_NAME_REQUIRED = "Product name is required";

        /**
         * Validation message for missing product description.
         */
        public static final String PRODUCT_DESCRIPTION_REQUIRED =
                "Product description is required";

        /**
         * Validation message for missing product category.
         */
        public static final String PRODUCT_CATEGORY_REQUIRED = "Product category is required";

        /**
         * Validation message for missing product price.
         */
        public static final String PRODUCT_PRICE_REQUIRED = "Product price is required";

        /**
         * Validation message for non-positive price values.
         */
        public static final String PRODUCT_PRICE_POSITIVE = "Price must be greater than 0";

        /**
         * Validation message for missing stock quantity.
         */
        public static final String STOCK_QUANTITY_REQUIRED = "Stock quantity is required";

        /**
         * Validation message for negative stock quantity.
         */
        public static final String STOCK_QUANTITY_NON_NEGATIVE =
                "Stock quantity cannot be negative";

        private Validation() {
            throw new AssertionError("Cannot instantiate utility class");
        }
    }

    /**
     * Logging message templates for consistent application logging.
     */
    public static final class Logger {

        /**
         * Log template for successful history retrieval.
         */
        public static final String HISTORY_RETRIEVED =
                "Retrieved history for product id={}: found {} events";

        /**
         * Log template for handling product creation events.
         */
        public static final String HANDLING_CREATED = "Handling CREATED event for product id={}";

        /**
         * Log template for handling product update events.
         */
        public static final String HANDLING_UPDATED = "Handling UPDATED event for product id={}";

        /**
         * Log template for handling product deletion events.
         */
        public static final String HANDLING_DELETED = "Handling DELETED event for product id={}";

        /**
         * Log template for successful Kafka message publishing.
         */
        public static final String MESSAGE_SENT = "Message sent to topic '{}': {}";

        /**
         * Log template for failed Kafka message publishing.
         */
        public static final String MESSAGE_SEND_FAILED = "Failed to send message to topic '{}': {}";

        /**
         * Log template for successful history persistence.
         */
        public static final String HISTORY_SAVED = "History saved: id={}";

        /**
         * Log template for Kafka deserialization errors.
         */
        public static final String ERROR_KAFKA_DESERIALIZE =
                "Error deserializing Kafka message from topic '{}'";

        /**
         * Log template for Kafka message processing errors.
         */
        public static final String ERROR_PROCESSING_KAFKA = "Error processing Kafka message";

        /**
         * Log template for product serialization errors.
         */
        public static final String ERROR_SERIALIZING = "Error serializing product to JSON";

        /**
         * Log template for successfully received Kafka messages.
         */
        public static final String KAFKA_MESSAGE_RECEIVED =
                "Kafka message received from topic '{}': {}";

        /**
         * Log template for unknown event type encounters.
         */
        public static final String UNKNOWN_EVENT_TYPE = "Unknown event type: {}";

        private Logger() {
            throw new AssertionError("Cannot instantiate utility class");
        }
    }

    /**
     * Search and filtering constants for product queries.
     */
    public static final class Search {

        /**
         * Query parameter name for search text.
         */
        public static final String PARAM_SEARCH = "search";

        /**
         * Query parameter name for product category.
         */
        public static final String PARAM_CATEGORY = "category";

        /**
         * Query parameter name for minimum price filter.
         */
        public static final String PARAM_MIN_PRICE = "minPrice";

        /**
         * Query parameter name for maximum price filter.
         */
        public static final String PARAM_MAX_PRICE = "maxPrice";

        /**
         * Query parameter name for minimum stock quantity filter.
         */
        public static final String PARAM_MIN_STOCK = "minStock";

        /**
         * Query parameter name for maximum stock quantity filter.
         */
        public static final String PARAM_MAX_STOCK = "maxStock";

        /**
         * Query parameter name for sort field specification.
         */
        public static final String PARAM_SORT_BY = "sortBy";

        /**
         * Query parameter name for sort direction (asc/desc).
         */
        public static final String PARAM_SORT_ORDER = "order";

        /**
         * Query parameter name for pagination page number.
         */
        public static final String PARAM_PAGE = "page";

        /**
         * Query parameter name for page size.
         */
        public static final String PARAM_SIZE = "size";

        /**
         * Sort field value: product name.
         */
        public static final String SORT_BY_NAME = "name";

        /**
         * Sort field value: product price.
         */
        public static final String SORT_BY_PRICE = "price";

        /**
         * Sort field value: stock quantity.
         */
        public static final String SORT_BY_STOCK = "stockQuantity";

        /**
         * Sort field value: event time (creation timestamp).
         */
        public static final String SORT_BY_CREATED = "eventTime";

        /**
         * Sort direction value: ascending order.
         */
        public static final String SORT_ASC = "asc";

        /**
         * Sort direction value: descending order.
         */
        public static final String SORT_DESC = "desc";

        /**
         * Default page number for paginated queries.
         */
        public static final int DEFAULT_PAGE = 0;

        /**
         * Default page size for paginated queries.
         */
        public static final int DEFAULT_SIZE = 20;

        /**
         * Maximum allowed page size.
         */
        public static final int MAX_SIZE = 100;

        private Search() {
            throw new AssertionError("Cannot instantiate utility class");
        }
    }
}
