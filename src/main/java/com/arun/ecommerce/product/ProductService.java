package com.arun.ecommerce.product;

import com.arun.ecommerce.entity.Product;
import com.arun.ecommerce.product.dto.PagedResponse;
import com.arun.ecommerce.product.dto.ProductRequest;
import com.arun.ecommerce.product.dto.ProductResponse;

public interface ProductService {

    // ── Paginated ─────────────────────────────────────────────
    PagedResponse<ProductResponse> getAllProducts(int page, int size,
                                                  String sortBy, String sortDir);

    PagedResponse<ProductResponse> searchProducts(String query,
                                                  int page, int size);

    // ── Single ────────────────────────────────────────────────
    ProductResponse getProductById(Long id);

    // ── Admin ─────────────────────────────────────────────────
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void            deleteProduct(Long id);

    // ── Internal ──────────────────────────────────────────────
    Product getProductEntityById(Long id);
    void    reduceStock(Long productId, Integer quantity);
    void    restoreStock(Long productId, Integer quantity);
}
