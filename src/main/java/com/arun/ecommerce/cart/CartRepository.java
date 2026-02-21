package com.arun.ecommerce.cart;

import com.arun.ecommerce.entity.CartItem;
import com.arun.ecommerce.entity.Product;
import com.arun.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartItem, Long> {

    List<CartItem>     findByUser(User user);
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    void               deleteByUser(User user);
}
