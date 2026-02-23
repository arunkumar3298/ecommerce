package com.arun.ecommerce.product;

import com.arun.ecommerce.entity.Product;
import com.arun.ecommerce.exception.ResourceNotFoundException;
import com.arun.ecommerce.product.dto.PagedResponse;
import com.arun.ecommerce.product.dto.ProductRequest;
import com.arun.ecommerce.product.dto.ProductResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository              productRepository;
    @Mock private RedisTemplate<String, String>  redisTemplate;
    @Mock private ObjectMapper                   objectMapper;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product        product;
    private ProductRequest productRequest;

    private static final String CACHE_PREFIX  = "products:page:";
    private static final String CACHE_PATTERN = "products:*";

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .name("iPhone 15")
                .description("Apple smartphone")
                .price(new BigDecimal("79999.00"))
                .stock(10)
                .category("Electronics")
                .imageUrl("iphone.jpg")
                .build();
        ReflectionTestUtils.setField(product, "id", 101L);

        productRequest = new ProductRequest();
        productRequest.setName("iPhone 15");
        productRequest.setDescription("Apple smartphone");
        productRequest.setPrice(new BigDecimal("79999.00"));
        productRequest.setStock(10);
        productRequest.setCategory("Electronics");
        productRequest.setImageUrl("iphone.jpg");

        // lenient → some tests throw before reaching Redis, that's fine ✅
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }


    // ── getAllProducts() — Cache MISS ─────────────────────────────

    @Test
    @DisplayName("getAllProducts: cache MISS should query DB and return response")
    void getAllProducts_cacheMiss_shouldQueryDB() {
        when(valueOperations.get(anyString())).thenReturn(null); // cache miss

        Page<Product> page = new PageImpl<>(
                List.of(product),
                PageRequest.of(0, 10, Sort.by("name").ascending()),
                1);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResponse<ProductResponse> response =
                productService.getAllProducts(0, 10, "name", "asc");

        assertNotNull(response);
        assertEquals(1,         response.getContent().size());
        assertEquals("iPhone 15", response.getContent().get(0).getName());
        assertEquals(0,         response.getPage());
        assertEquals(1,         response.getTotalElements());
        verify(productRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("getAllProducts: cache MISS should store result in Redis")
    void getAllProducts_cacheMiss_shouldStoreInRedis() throws Exception {
        when(valueOperations.get(anyString())).thenReturn(null);

        Page<Product> page = new PageImpl<>(List.of(product),
                PageRequest.of(0, 10), 1);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"cached\":true}");

        productService.getAllProducts(0, 10, "name", "asc");

        // Redis set() must be called to store the result
        verify(valueOperations, times(1)).set(
                anyString(), anyString(), anyLong(), any());
    }

    // ── getAllProducts() — Cache HIT ──────────────────────────────

    @Test
    @DisplayName("getAllProducts: cache HIT should return cached data without DB call")
    void getAllProducts_cacheHit_shouldNotQueryDB() throws Exception {
        String cachedJson = "{\"content\":[],\"page\":0,\"size\":10," +
                "\"totalElements\":0,\"totalPages\":0,\"last\":true}";

        when(valueOperations.get(anyString())).thenReturn(cachedJson);

        PagedResponse<ProductResponse> cachedResponse =
                new PagedResponse<>(List.of(), 0, 10, 0L, 0, true);
        when(objectMapper.readValue(eq(cachedJson), any(
                com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(cachedResponse);

        PagedResponse<ProductResponse> response =
                productService.getAllProducts(0, 10, "name", "asc");

        assertNotNull(response);
        // DB must NOT be called on cache hit
        verify(productRepository, never()).findAll(any(Pageable.class));
    }

    // ── searchProducts() ─────────────────────────────────────────

    @Test
    @DisplayName("searchProducts: should return matching products")
    void searchProducts_shouldReturnMatchingProducts() {
        Page<Product> page = new PageImpl<>(List.of(product),
                PageRequest.of(0, 10), 1);

        when(productRepository
                .findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                        eq("iPhone"), eq("iPhone"), any(Pageable.class)))
                .thenReturn(page);

        PagedResponse<ProductResponse> response =
                productService.searchProducts("iPhone", 0, 10);

        assertEquals(1, response.getContent().size());
        assertEquals("iPhone 15", response.getContent().get(0).getName());
    }

    @Test
    @DisplayName("searchProducts: no results should return empty paged response")
    void searchProducts_noResults_shouldReturnEmpty() {
        Page<Product> emptyPage = new PageImpl<>(List.of(),
                PageRequest.of(0, 10), 0);

        when(productRepository
                .findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                        anyString(), anyString(), any(Pageable.class)))
                .thenReturn(emptyPage);

        PagedResponse<ProductResponse> response =
                productService.searchProducts("xyz", 0, 10);

        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotalElements());
    }

    // ── getProductById() ─────────────────────────────────────────

    @Test
    @DisplayName("getProductById: valid ID should return ProductResponse")
    void getProductById_validId_shouldReturnResponse() {
        when(productRepository.findById(101L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(101L);

        assertNotNull(response);
        assertEquals(101L,         response.getId());
        assertEquals("iPhone 15",  response.getName());
        assertEquals(new BigDecimal("79999.00"), response.getPrice());
        assertEquals(10,           response.getStock());
    }

    @Test
    @DisplayName("getProductById: invalid ID should throw ResourceNotFoundException")
    void getProductById_invalidId_shouldThrowException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductById(999L));
    }

    // ── createProduct() ───────────────────────────────────────────

    @Test
    @DisplayName("createProduct: should save product and return response")
    void createProduct_shouldSaveAndReturn() {
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(redisTemplate.keys(CACHE_PATTERN)).thenReturn(Set.of());

        ProductResponse response = productService.createProduct(productRequest);

        assertNotNull(response);
        assertEquals("iPhone 15", response.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("createProduct: should evict Redis cache after saving")
    void createProduct_shouldEvictCache() {
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(redisTemplate.keys(CACHE_PATTERN)).thenReturn(
                Set.of("products:page:0:10:name:asc"));

        productService.createProduct(productRequest);

        verify(redisTemplate, times(1))
                .delete(anyCollection());
    }

    // ── updateProduct() ───────────────────────────────────────────

    @Test
    @DisplayName("updateProduct: should update fields and return response")
    void updateProduct_shouldUpdateFields() {
        productRequest.setName("iPhone 15 Pro");
        productRequest.setPrice(new BigDecimal("99999.00"));

        when(productRepository.findById(101L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(redisTemplate.keys(CACHE_PATTERN)).thenReturn(Set.of());

        ProductResponse response = productService.updateProduct(101L, productRequest);

        assertNotNull(response);
        assertEquals("iPhone 15 Pro",          product.getName());
        assertEquals(new BigDecimal("99999.00"), product.getPrice());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("updateProduct: product not found should throw ResourceNotFoundException")
    void updateProduct_notFound_shouldThrowException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(999L, productRequest));

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct: should evict Redis cache after update")
    void updateProduct_shouldEvictCache() {
        when(productRepository.findById(101L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(redisTemplate.keys(CACHE_PATTERN))
                .thenReturn(Set.of("products:page:0:10:name:asc"));

        productService.updateProduct(101L, productRequest);

        verify(redisTemplate, times(1)).delete(anyCollection());
    }

    // ── deleteProduct() ───────────────────────────────────────────

    @Test
    @DisplayName("deleteProduct: valid ID should delete product")
    void deleteProduct_validId_shouldDelete() {
        when(productRepository.existsById(101L)).thenReturn(true);
        when(redisTemplate.keys(CACHE_PATTERN)).thenReturn(Set.of());

        productService.deleteProduct(101L);

        verify(productRepository, times(1)).deleteById(101L);
    }

    @Test
    @DisplayName("deleteProduct: product not found should throw ResourceNotFoundException")
    void deleteProduct_notFound_shouldThrowException() {
        when(productRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> productService.deleteProduct(999L));

        verify(productRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteProduct: should evict Redis cache after deletion")
    void deleteProduct_shouldEvictCache() {
        when(productRepository.existsById(101L)).thenReturn(true);
        when(redisTemplate.keys(CACHE_PATTERN))
                .thenReturn(Set.of("products:page:0:10:name:asc"));

        productService.deleteProduct(101L);

        verify(redisTemplate, times(1)).delete(anyCollection());
    }

    // ── reduceStock() ─────────────────────────────────────────────

    @Test
    @DisplayName("reduceStock: sufficient stock should reduce correctly")
    void reduceStock_sufficientStock_shouldReduce() {
        when(productRepository.findById(101L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(redisTemplate.keys(CACHE_PATTERN)).thenReturn(Set.of());

        productService.reduceStock(101L, 3);

        assertEquals(7, product.getStock()); // 10 - 3 = 7
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("reduceStock: insufficient stock should throw IllegalArgumentException")
    void reduceStock_insufficientStock_shouldThrowException() {
        when(productRepository.findById(101L)).thenReturn(Optional.of(product));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> productService.reduceStock(101L, 15)); // 15 > 10

        assertTrue(ex.getMessage().contains("Insufficient stock"));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("reduceStock: exact stock amount should reduce to zero")
    void reduceStock_exactStock_shouldReduceToZero() {
        when(productRepository.findById(101L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(redisTemplate.keys(CACHE_PATTERN)).thenReturn(Set.of());

        productService.reduceStock(101L, 10); // exactly 10

        assertEquals(0, product.getStock());
    }

    // ── restoreStock() ────────────────────────────────────────────

    @Test
    @DisplayName("restoreStock: should increase stock correctly")
    void restoreStock_shouldIncreaseStock() {
        when(productRepository.findById(101L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(redisTemplate.keys(CACHE_PATTERN)).thenReturn(Set.of());

        productService.restoreStock(101L, 5);

        assertEquals(15, product.getStock()); // 10 + 5 = 15
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("restoreStock: should evict Redis cache after restore")
    void restoreStock_shouldEvictCache() {
        when(productRepository.findById(101L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(redisTemplate.keys(CACHE_PATTERN))
                .thenReturn(Set.of("products:page:0:10:name:asc"));

        productService.restoreStock(101L, 5);

        verify(redisTemplate, times(1)).delete(anyCollection());
    }
}
