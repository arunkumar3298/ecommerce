package com.arun.ecommerce.order;

import com.arun.ecommerce.entity.OrderItem;
import com.arun.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
}
