package com.example.productservice.kafka;

import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductConsumerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProductConsumer productConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConsumeValidMessage() throws Exception {
        String jsonMessage = "{\"id\":1,\"name\":\"Apple\",\"description\":\"Red Apple\",\"price\":1.5,\"eventType\":\"CREATED\"}";
        Product productEvent = new Product();
        productEvent.setId(1L);
        productEvent.setName("Apple");
        productEvent.setDescription("Red Apple");
        productEvent.setPrice(1.5);
        productEvent.setEventType("CREATED");

        when(objectMapper.readValue(jsonMessage, Product.class)).thenReturn(productEvent);

        productConsumer.consume(jsonMessage);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(1)).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();
        assertEquals("Apple", savedProduct.getName());
        assertEquals("Red Apple", savedProduct.getDescription());
        assertEquals(1.5, savedProduct.getPrice());
        assertEquals("CREATED", savedProduct.getEventType());
        assertEquals(1L, savedProduct.getOriginalProductId());
        assertNotNull(savedProduct.getEventTime());
    }

    @Test
    void testConsumeWithEventTypeUpdate() throws Exception {
        String jsonMessage = "{\"id\":5,\"name\":\"Banana\",\"description\":\"Yellow Banana\",\"price\":0.75,\"eventType\":\"UPDATED\"}";
        Product productEvent = new Product();
        productEvent.setId(5L);
        productEvent.setName("Banana");
        productEvent.setDescription("Yellow Banana");
        productEvent.setPrice(0.75);
        productEvent.setEventType("UPDATED");

        when(objectMapper.readValue(jsonMessage, Product.class)).thenReturn(productEvent);

        productConsumer.consume(jsonMessage);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(1)).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();
        assertEquals("UPDATED", savedProduct.getEventType());
        assertEquals(5L, savedProduct.getOriginalProductId());
    }

    @Test
    void testConsumeWithEventTypeDeleted() throws Exception {
        String jsonMessage = "{\"id\":10,\"name\":\"Orange\",\"description\":\"Orange Fruit\",\"price\":2.0,\"eventType\":\"DELETED\"}";
        Product productEvent = new Product();
        productEvent.setId(10L);
        productEvent.setName("Orange");
        productEvent.setDescription("Orange Fruit");
        productEvent.setPrice(2.0);
        productEvent.setEventType("DELETED");

        when(objectMapper.readValue(jsonMessage, Product.class)).thenReturn(productEvent);

        productConsumer.consume(jsonMessage);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(1)).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();
        assertEquals("DELETED", savedProduct.getEventType());
    }

    @Test
    void testConsumeWithInvalidJson() throws Exception {
        String invalidJsonMessage = "{invalid json}";

        when(objectMapper.readValue(invalidJsonMessage, Product.class))
                .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "Invalid JSON"));

        productConsumer.consume(invalidJsonMessage);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testConsumeWithEmptyMessage() throws Exception {
        String emptyMessage = "";

        when(objectMapper.readValue(emptyMessage, Product.class))
                .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "No content"));

        productConsumer.consume(emptyMessage);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testConsumeEventTimeIsSet() throws Exception {
        String jsonMessage = "{\"id\":3,\"name\":\"Grape\",\"description\":\"Purple Grapes\",\"price\":3.0,\"eventType\":\"CREATED\"}";
        Product productEvent = new Product();
        productEvent.setId(3L);
        productEvent.setName("Grape");
        productEvent.setDescription("Purple Grapes");
        productEvent.setPrice(3.0);
        productEvent.setEventType("CREATED");
        productEvent.setEventTime(null);

        when(objectMapper.readValue(jsonMessage, Product.class)).thenReturn(productEvent);

        LocalDateTime beforeConsume = LocalDateTime.now();
        productConsumer.consume(jsonMessage);
        LocalDateTime afterConsume = LocalDateTime.now();

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(1)).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();
        assertNotNull(savedProduct.getEventTime());
        assertTrue(savedProduct.getEventTime().isAfter(beforeConsume.minusSeconds(1)));
        assertTrue(savedProduct.getEventTime().isBefore(afterConsume.plusSeconds(1)));
    }

    @Test
    void testConsumePreservesProductFields() throws Exception {
        String jsonMessage = "{\"id\":7,\"name\":\"Mango\",\"description\":\"Sweet Mango\",\"price\":2.5,\"eventType\":\"CREATED\"}";
        Product productEvent = new Product();
        productEvent.setId(7L);
        productEvent.setName("Mango");
        productEvent.setDescription("Sweet Mango");
        productEvent.setPrice(2.5);
        productEvent.setEventType("CREATED");

        when(objectMapper.readValue(jsonMessage, Product.class)).thenReturn(productEvent);

        productConsumer.consume(jsonMessage);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(1)).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();
        assertEquals("Mango", savedProduct.getName());
        assertEquals("Sweet Mango", savedProduct.getDescription());
        assertEquals(2.5, savedProduct.getPrice());
        assertEquals("CREATED", savedProduct.getEventType());
        assertEquals(7L, savedProduct.getOriginalProductId());
        assertNotNull(savedProduct.getEventTime());
    }

    @Test
    void testConsumeHandlesRepositorySaveException() throws Exception {
        String jsonMessage = "{\"id\":1,\"name\":\"Apple\",\"description\":\"Red Apple\",\"price\":1.5,\"eventType\":\"CREATED\"}";
        Product productEvent = new Product();
        productEvent.setId(1L);
        productEvent.setName("Apple");
        productEvent.setDescription("Red Apple");
        productEvent.setPrice(1.5);
        productEvent.setEventType("CREATED");

        when(objectMapper.readValue(jsonMessage, Product.class)).thenReturn(productEvent);
        when(productRepository.save(any(Product.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertDoesNotThrow(() -> productConsumer.consume(jsonMessage));

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testListenWithCreatedEventType() throws Exception {
        String jsonMessage = "{\"id\":1,\"name\":\"Laptop\",\"description\":\"Gaming Laptop\",\"category\":\"Electronics\",\"price\":999.99,\"stockQuantity\":10,\"eventType\":\"CREATED\"}";
        Product productEvent = new Product();
        productEvent.setId(1L);
        productEvent.setName("Laptop");
        productEvent.setDescription("Gaming Laptop");
        productEvent.setCategory("Electronics");
        productEvent.setPrice(999.99);
        productEvent.setStockQuantity(10);
        productEvent.setEventType("CREATED");

        when(objectMapper.readValue(jsonMessage, Product.class)).thenReturn(productEvent);
        when(productRepository.save(any(Product.class))).thenReturn(productEvent);

        productConsumer.listen(jsonMessage);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(2)).save(productCaptor.capture());

        assertEquals("Laptop", productCaptor.getValue().getName());
        assertEquals("CREATED", productCaptor.getValue().getEventType());
    }

    @Test
    void testListenWithUpdatedEventType() throws Exception {
        String jsonMessage = "{\"id\":2,\"name\":\"Phone\",\"description\":\"Smartphone\",\"category\":\"Mobile\",\"price\":599.99,\"stockQuantity\":50,\"eventType\":\"UPDATED\"}";
        Product productEvent = new Product();
        productEvent.setId(2L);
        productEvent.setName("Phone");
        productEvent.setDescription("Smartphone");
        productEvent.setCategory("Mobile");
        productEvent.setPrice(599.99);
        productEvent.setStockQuantity(50);
        productEvent.setEventType("UPDATED");

        Product existingProduct = new Product();
        existingProduct.setId(2L);
        existingProduct.setName("Old Phone");
        existingProduct.setDescription("Old Description");
        existingProduct.setPrice(499.99);

        when(objectMapper.readValue(jsonMessage, Product.class)).thenReturn(productEvent);
        when(productRepository.findById(2L)).thenReturn(java.util.Optional.of(existingProduct));

        productConsumer.listen(jsonMessage);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(2)).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();
        assertEquals("Phone", savedProduct.getName());
        assertEquals("Smartphone", savedProduct.getDescription());
        assertEquals(599.99, savedProduct.getPrice());
        assertEquals(50, savedProduct.getStockQuantity());
    }

    @Test
    void testListenWithUpdatedEventTypeProductNotFound() throws Exception {
        String jsonMessage = "{\"id\":999,\"name\":\"NonExistent\",\"description\":\"Does not exist\",\"category\":\"Test\",\"price\":10.0,\"stockQuantity\":5,\"eventType\":\"UPDATED\"}";
        Product productEvent = new Product();
        productEvent.setId(999L);
        productEvent.setName("NonExistent");
        productEvent.setDescription("Does not exist");
        productEvent.setCategory("Test");
        productEvent.setPrice(10.0);
        productEvent.setStockQuantity(5);
        productEvent.setEventType("UPDATED");

        when(objectMapper.readValue(jsonMessage, Product.class)).thenReturn(productEvent);
        when(productRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        productConsumer.listen(jsonMessage);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(1)).save(productCaptor.capture());
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void testListenWithDeletedEventType() throws Exception {
        String jsonMessage = "{\"id\":5,\"name\":\"Tablet\",\"description\":\"Tablet Device\",\"category\":\"Electronics\",\"price\":299.99,\"stockQuantity\":20,\"eventType\":\"DELETED\"}";
        Product productEvent = new Product();
        productEvent.setId(5L);
        productEvent.setName("Tablet");
        productEvent.setDescription("Tablet Device");
        productEvent.setCategory("Electronics");
        productEvent.setPrice(299.99);
        productEvent.setStockQuantity(20);
        productEvent.setEventType("DELETED");

        when(objectMapper.readValue(jsonMessage, Product.class)).thenReturn(productEvent);

        productConsumer.listen(jsonMessage);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(1)).save(productCaptor.capture());
        verify(productRepository, times(1)).deleteById(5L);
    }

    @Test
    void testListenWithUnknownEventType() throws Exception {
        String jsonMessage = "{\"id\":3,\"name\":\"Monitor\",\"description\":\"4K Monitor\",\"category\":\"Electronics\",\"price\":399.99,\"stockQuantity\":15,\"eventType\":\"UNKNOWN\"}";
        Product productEvent = new Product();
        productEvent.setId(3L);
        productEvent.setName("Monitor");
        productEvent.setDescription("4K Monitor");
        productEvent.setCategory("Electronics");
        productEvent.setPrice(399.99);
        productEvent.setStockQuantity(15);
        productEvent.setEventType("UNKNOWN");

        when(objectMapper.readValue(jsonMessage, Product.class)).thenReturn(productEvent);

        productConsumer.listen(jsonMessage);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(1)).save(productCaptor.capture());
    }

    @Test
    void testListenSetsEventTimeCorrectly() throws Exception {
        String jsonMessage = "{\"id\":4,\"name\":\"Keyboard\",\"description\":\"Mechanical Keyboard\",\"category\":\"Peripherals\",\"price\":129.99,\"stockQuantity\":30,\"eventType\":\"CREATED\"}";
        Product productEvent = new Product();
        productEvent.setId(4L);
        productEvent.setName("Keyboard");
        productEvent.setDescription("Mechanical Keyboard");
        productEvent.setCategory("Peripherals");
        productEvent.setPrice(129.99);
        productEvent.setStockQuantity(30);
        productEvent.setEventType("CREATED");

        when(objectMapper.readValue(jsonMessage, Product.class)).thenReturn(productEvent);

        LocalDateTime beforeListen = LocalDateTime.now();
        productConsumer.listen(jsonMessage);
        LocalDateTime afterListen = LocalDateTime.now();

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(2)).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();
        assertNotNull(savedProduct.getEventTime());
        assertTrue(savedProduct.getEventTime().isAfter(beforeListen.minusSeconds(1)));
        assertTrue(savedProduct.getEventTime().isBefore(afterListen.plusSeconds(1)));
    }

    @Test
    void testListenCreatesHistoryRecord() throws Exception {
        String jsonMessage = "{\"id\":6,\"name\":\"Mouse\",\"description\":\"Wireless Mouse\",\"category\":\"Peripherals\",\"price\":49.99,\"stockQuantity\":100,\"eventType\":\"CREATED\"}";
        Product productEvent = new Product();
        productEvent.setId(6L);
        productEvent.setName("Mouse");
        productEvent.setDescription("Wireless Mouse");
        productEvent.setCategory("Peripherals");
        productEvent.setPrice(49.99);
        productEvent.setStockQuantity(100);
        productEvent.setEventType("CREATED");

        when(objectMapper.readValue(jsonMessage, Product.class)).thenReturn(productEvent);

        productConsumer.listen(jsonMessage);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(2)).save(productCaptor.capture());

        List<Product> allSavedProducts = productCaptor.getAllValues();
        Product history = allSavedProducts.get(0);

        assertEquals("Mouse", history.getName());
        assertEquals("Wireless Mouse", history.getDescription());
        assertEquals("Peripherals", history.getCategory());
        assertEquals(49.99, history.getPrice());
        assertEquals(100, history.getStockQuantity());
        assertEquals("CREATED", history.getEventType());
        assertEquals(6L, history.getOriginalProductId());
        assertNotNull(history.getEventTime());
    }

    @Test
    void testListenWithInvalidJson() throws Exception {
        String invalidJsonMessage = "{invalid}";

        when(objectMapper.readValue(invalidJsonMessage, Product.class))
                .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "Invalid JSON"));

        productConsumer.listen(invalidJsonMessage);

        verify(productRepository, never()).save(any(Product.class));
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void testListenWithEmptyMessage() throws Exception {
        String emptyMessage = "";

        when(objectMapper.readValue(emptyMessage, Product.class))
                .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "No content"));

        productConsumer.listen(emptyMessage);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testListenHandlesRepositoryException() throws Exception {
        String jsonMessage = "{\"id\":7,\"name\":\"Headphones\",\"description\":\"Wireless Headphones\",\"category\":\"Audio\",\"price\":199.99,\"stockQuantity\":25,\"eventType\":\"CREATED\"}";
        Product productEvent = new Product();
        productEvent.setId(7L);
        productEvent.setName("Headphones");
        productEvent.setDescription("Wireless Headphones");
        productEvent.setCategory("Audio");
        productEvent.setPrice(199.99);
        productEvent.setStockQuantity(25);
        productEvent.setEventType("CREATED");

        when(objectMapper.readValue(jsonMessage, Product.class)).thenReturn(productEvent);
        when(productRepository.save(any(Product.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertDoesNotThrow(() -> productConsumer.listen(jsonMessage));

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testListenPreservesAllProductFields() throws Exception {
        String jsonMessage = "{\"id\":8,\"name\":\"Monitor\",\"description\":\"UltraWide Monitor\",\"category\":\"Displays\",\"price\":349.99,\"stockQuantity\":12,\"eventType\":\"UPDATED\"}";
        Product productEvent = new Product();
        productEvent.setId(8L);
        productEvent.setName("Monitor");
        productEvent.setDescription("UltraWide Monitor");
        productEvent.setCategory("Displays");
        productEvent.setPrice(349.99);
        productEvent.setStockQuantity(12);
        productEvent.setEventType("UPDATED");

        Product existingProduct = new Product();
        existingProduct.setId(8L);
        existingProduct.setName("Old Monitor");
        existingProduct.setPrice(299.99);

        when(objectMapper.readValue(jsonMessage, Product.class)).thenReturn(productEvent);
        when(productRepository.findById(8L)).thenReturn(java.util.Optional.of(existingProduct));

        productConsumer.listen(jsonMessage);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(2)).save(productCaptor.capture());

        Product updatedProduct = productCaptor.getValue();
        assertEquals("Monitor", updatedProduct.getName());
        assertEquals("UltraWide Monitor", updatedProduct.getDescription());
        assertEquals("Displays", updatedProduct.getCategory());
        assertEquals(349.99, updatedProduct.getPrice());
        assertEquals(12, updatedProduct.getStockQuantity());
    }
}
