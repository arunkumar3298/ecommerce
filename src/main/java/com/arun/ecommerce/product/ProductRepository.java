package com.arun.ecommerce.product;

import com.arun.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // ── Paginated search ──────────────────────────────────────
    Page<Product> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String name, String category, Pageable pageable);
}
