package com.arun.ecommerce.product;

import com.arun.ecommerce.entity.Product;
import com.arun.ecommerce.exception.ResourceNotFoundException;
import com.arun.ecommerce.product.dto.PagedResponse;
import com.arun.ecommerce.product.dto.ProductRequest;
import com.arun.ecommerce.product.dto.ProductResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private static final String CACHE_PREFIX     = "products:page:";
    private static final String CACHE_PATTERN    = "products:*";
    private static final int    CACHE_TTL_SECONDS = 600; // 10 minutes

    private final ProductRepository             productRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper                  objectMapper;

    public ProductServiceImpl(ProductRepository productRepository,
                              RedisTemplate<String, String> redisTemplate,
                              ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.redisTemplate     = redisTemplate;
        this.objectMapper      = objectMapper;
    }

    // ── Get All Products (Paginated + Cached) ─────────────────

    @Override
    public PagedResponse<ProductResponse> getAllProducts(int page, int size,
                                                         String sortBy,
                                                         String sortDir) {
        // Cache key is unique per page/size/sort combination
        String cacheKey = CACHE_PREFIX + page + ":" + size
                + ":" + sortBy + ":" + sortDir;

        // 1. Check Redis cache first
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached,
                        new TypeReference<PagedResponse<ProductResponse>>() {});
            } catch (Exception e) {
                // Cache corrupted → fall through to DB
            }
        }

        // 2. Cache MISS → query DB
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findAll(pageable);

        List<ProductResponse> content = productPage.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        PagedResponse<ProductResponse> response = new PagedResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast());

        // 3. Store in Redis with 10 min TTL
        try {
            redisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(response),
                    CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            // Cache write failed — DB result still returned
        }

        return response;
    }

    // ── Search Products (Paginated) ───────────────────────────

    @Override
    public PagedResponse<ProductResponse> searchProducts(String query,
                                                         int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("name").ascending());

        Page<Product> productPage = productRepository
                .findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                        query, query, pageable);

        List<ProductResponse> content = productPage.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast());
    }

    // ── Get Single Product ────────────────────────────────────

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));
        return toResponse(product);
    }

    // ── Write Operations (ADMIN only) ─────────────────────────

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .build();

        ProductResponse response = toResponse(productRepository.save(product));
        evictProductCache();
        return response;
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());

        ProductResponse response = toResponse(productRepository.save(product));
        evictProductCache();
        return response;
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        evictProductCache();
    }

    // ── Internal ──────────────────────────────────────────────

    @Override
    public Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));
    }

    @Override
    @Transactional
    public void reduceStock(Long productId, Integer quantity) {
        Product product = getProductEntityById(productId);
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException(
                    "Insufficient stock for: " + product.getName()
                            + ". Available: " + product.getStock());
        }
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        evictProductCache();
    }

    @Override
    @Transactional
    public void restoreStock(Long productId, Integer quantity) {
        Product product = getProductEntityById(productId);
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
        evictProductCache();
    }

    // ── Private Helpers ───────────────────────────────────────

    // Deletes ALL page cache keys when any product changes
    private void evictProductCache() {
        Set<String> keys = redisTemplate.keys(CACHE_PATTERN);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
