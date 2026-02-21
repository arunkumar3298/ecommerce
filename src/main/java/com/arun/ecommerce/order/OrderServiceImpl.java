package com.arun.ecommerce.order;

import com.arun.ecommerce.cart.CartService;
import com.arun.ecommerce.entity.*;
import com.arun.ecommerce.entity.enums.OrderStatus;
import com.arun.ecommerce.entity.enums.PaymentStatus;
import com.arun.ecommerce.exception.ResourceNotFoundException;
import com.arun.ecommerce.messaging.EmailPublisher;
import com.arun.ecommerce.order.dto.OrderItemResponse;
import com.arun.ecommerce.order.dto.OrderRequest;
import com.arun.ecommerce.order.dto.OrderResponse;
import com.arun.ecommerce.order.dto.OrderStatusUpdateRequest;
import com.arun.ecommerce.product.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository     orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService         cartService;
    private final ProductService      productService;
    private final EmailPublisher      emailPublisher;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            CartService cartService,
                            ProductService productService,
                            EmailPublisher emailPublisher) {
        this.orderRepository     = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartService         = cartService;
        this.productService      = productService;
        this.emailPublisher      = emailPublisher;
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(User user, OrderRequest request) {

        List<CartItem> cartItems = cartService.getCartItemEntities(user);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cart is empty. Add items before placing order.");
        }

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for: " + product.getName()
                                + ". Available: " + product.getStock());
            }
        }

        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status(OrderStatus.PLACED)
                .paymentStatus(PaymentStatus.PENDING)
                .streetAddress(request.getStreetAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .phone(request.getPhone())
                .build();

        Order savedOrder = orderRepository.save(order);

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(cartItem.getProduct().getPrice())
                    .build();

            orderItemRepository.save(orderItem);
            productService.reduceStock(
                    cartItem.getProduct().getId(),
                    cartItem.getQuantity());
        }

        cartService.clearCart(user);

        emailPublisher.publishOrderConfirmationEmail(
                user.getEmail(),
                savedOrder.getId(),
                totalAmount);

        return toResponse(savedOrder);
    }

    @Override
    public List<OrderResponse> getMyOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse getOrderById(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "Unauthorized: This order does not belong to you");
        }

        return toResponse(order);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId,
                                           OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cannot update a cancelled order");
        }

        order.setStatus(request.getStatus());
        return toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "Unauthorized: This order does not belong to you");
        }

        if (order.getStatus() != OrderStatus.PLACED) {
            throw new IllegalArgumentException(
                    "Order cannot be cancelled. Current status: "
                            + order.getStatus());
        }

        List<OrderItem> items = orderItemRepository.findByOrder(order);
        for (OrderItem item : items) {
            productService.restoreStock(
                    item.getProduct().getId(),
                    item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    @Override
    public Order getOrderEntityById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));
    }

    @Override
    @Transactional
    public void markAsPaid(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    // ── Private Mapper ────────────────────────────────────────

    private OrderResponse toResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrder(order);

        List<OrderItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userEmail(order.getUser().getEmail())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .orderItems(itemResponses)
                .createdAt(order.getCreatedAt())
                .streetAddress(order.getStreetAddress())
                .city(order.getCity())
                .state(order.getState())
                .pincode(order.getPincode())
                .phone(order.getPhone())
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        BigDecimal subtotal = item.getPriceAtPurchase()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return OrderItemResponse.builder()
                .orderItemId(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productCategory(item.getProduct().getCategory())
                .quantity(item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase())
                .subtotal(subtotal)
                .build();
    }
}
