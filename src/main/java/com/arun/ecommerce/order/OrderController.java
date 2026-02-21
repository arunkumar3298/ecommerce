package com.arun.ecommerce.order;

import com.arun.ecommerce.entity.User;
import com.arun.ecommerce.order.dto.OrderRequest;
import com.arun.ecommerce.order.dto.OrderResponse;
import com.arun.ecommerce.order.dto.OrderStatusUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/place")
    public ResponseEntity<OrderResponse> placeOrder(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(
                orderService.placeOrder(currentUser, request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(orderService.getMyOrders(currentUser));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(
                orderService.getOrderById(currentUser, orderId));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(
                orderService.cancelOrder(currentUser, orderId));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(
                orderService.updateOrderStatus(orderId, request));
    }
}
