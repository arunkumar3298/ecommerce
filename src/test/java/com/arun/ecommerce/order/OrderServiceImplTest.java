package com.arun.ecommerce.order;

import com.arun.ecommerce.cart.CartService;
import com.arun.ecommerce.entity.*;
import com.arun.ecommerce.entity.enums.OrderStatus;
import com.arun.ecommerce.entity.enums.PaymentStatus;
import com.arun.ecommerce.entity.enums.Role;
import com.arun.ecommerce.exception.ResourceNotFoundException;
import com.arun.ecommerce.messaging.EmailPublisher;
import com.arun.ecommerce.order.dto.OrderRequest;
import com.arun.ecommerce.order.dto.OrderResponse;
import com.arun.ecommerce.order.dto.OrderStatusUpdateRequest;
import com.arun.ecommerce.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository     orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private CartService         cartService;
    @Mock private ProductService      productService;
    @Mock private EmailPublisher      emailPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User        user;
    private Product     product;
    private CartItem    cartItem;
    private Order       order;
    private OrderItem   orderItem;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        // ── User ──────────────────────────────────────────────────
        user = User.builder()
                .name("Arun Kumar")
                .email("arun@test.com")
                .password("encoded")
                .role(Role.USER)
                .isVerified(true)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        // ── Product ───────────────────────────────────────────────
        product = Product.builder()
                .name("iPhone 15")
                .description("Apple smartphone")
                .price(new BigDecimal("79999.00"))
                .stock(10)
                .category("Electronics")
                .imageUrl("iphone.jpg")
                .build();
        ReflectionTestUtils.setField(product, "id", 101L);

        // ── CartItem ──────────────────────────────────────────────
        cartItem = CartItem.builder()
                .user(user)
                .product(product)
                .quantity(2)
                .build();
        ReflectionTestUtils.setField(cartItem, "id", 201L);

        // ── Order ─────────────────────────────────────────────────
        order = Order.builder()
                .user(user)
                .totalAmount(new BigDecimal("159998.00"))
                .status(OrderStatus.PLACED)
                .paymentStatus(PaymentStatus.PENDING)
                .streetAddress("123 MG Road")
                .city("Vijayawada")
                .state("Andhra Pradesh")
                .pincode("520001")
                .phone("9876543210")
                .build();
        ReflectionTestUtils.setField(order, "id", 301L);

        // ── OrderItem ─────────────────────────────────────────────
        orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(2)
                .priceAtPurchase(new BigDecimal("79999.00"))
                .build();
        ReflectionTestUtils.setField(orderItem, "id", 401L);

        // ── OrderRequest ──────────────────────────────────────────
        orderRequest = new OrderRequest();
        orderRequest.setStreetAddress("123 MG Road");
        orderRequest.setCity("Vijayawada");
        orderRequest.setState("Andhra Pradesh");
        orderRequest.setPincode("520001");
        orderRequest.setPhone("9876543210");
    }

    // ── placeOrder() ──────────────────────────────────────────────

    @Test
    @DisplayName("placeOrder: should save order and return response")
    void placeOrder_validCart_shouldSaveOrder() {
        when(cartService.getCartItemEntities(user)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);
        when(orderItemRepository.findByOrder(order)).thenReturn(List.of(orderItem));

        OrderResponse response = orderService.placeOrder(user, orderRequest);

        assertNotNull(response);
        assertEquals(301L,           response.getId());
        assertEquals("arun@test.com", response.getUserEmail());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("placeOrder: total amount should be sum of price × quantity")
    void placeOrder_shouldCalculateTotalAmountCorrectly() {
        when(cartService.getCartItemEntities(user)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);
        when(orderItemRepository.findByOrder(order)).thenReturn(List.of(orderItem));

        OrderResponse response = orderService.placeOrder(user, orderRequest);

        // 79999.00 × 2 = 159998.00
        assertEquals(0,
                new BigDecimal("159998.00").compareTo(response.getTotalAmount()));
    }

    @Test
    @DisplayName("placeOrder: should reduce stock for each product")
    void placeOrder_shouldReduceStock() {
        when(cartService.getCartItemEntities(user)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);
        when(orderItemRepository.findByOrder(order)).thenReturn(List.of(orderItem));

        orderService.placeOrder(user, orderRequest);

        verify(productService, times(1)).reduceStock(101L, 2);
    }

    @Test
    @DisplayName("placeOrder: should clear cart after placing order")
    void placeOrder_shouldClearCartAfterOrder() {
        when(cartService.getCartItemEntities(user)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);
        when(orderItemRepository.findByOrder(order)).thenReturn(List.of(orderItem));

        orderService.placeOrder(user, orderRequest);

        verify(cartService, times(1)).clearCart(user);
    }

    @Test
    @DisplayName("placeOrder: should publish order confirmation email")
    void placeOrder_shouldPublishConfirmationEmail() {
        when(cartService.getCartItemEntities(user)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);
        when(orderItemRepository.findByOrder(order)).thenReturn(List.of(orderItem));

        orderService.placeOrder(user, orderRequest);

        verify(emailPublisher, times(1))
                .publishOrderConfirmationEmail(
                        eq("arun@test.com"),
                        eq(301L),
                        any(BigDecimal.class));
    }

    @Test
    @DisplayName("placeOrder: empty cart should throw IllegalArgumentException")
    void placeOrder_emptyCart_shouldThrowException() {
        when(cartService.getCartItemEntities(user)).thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.placeOrder(user, orderRequest));

        assertTrue(ex.getMessage().contains("Cart is empty"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("placeOrder: insufficient stock should throw IllegalArgumentException")
    void placeOrder_insufficientStock_shouldThrowException() {
        product.setStock(1); // only 1 available, but cart has 2

        when(cartService.getCartItemEntities(user)).thenReturn(List.of(cartItem));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.placeOrder(user, orderRequest));

        assertTrue(ex.getMessage().contains("Insufficient stock"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("placeOrder: initial order status should be PLACED")
    void placeOrder_initialStatusShouldBePlaced() {
        when(cartService.getCartItemEntities(user)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);
        when(orderItemRepository.findByOrder(order)).thenReturn(List.of(orderItem));

        OrderResponse response = orderService.placeOrder(user, orderRequest);

        assertEquals("PLACED", response.getStatus());
    }

    @Test
    @DisplayName("placeOrder: initial payment status should be PENDING")
    void placeOrder_initialPaymentStatusShouldBePending() {
        when(cartService.getCartItemEntities(user)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);
        when(orderItemRepository.findByOrder(order)).thenReturn(List.of(orderItem));

        OrderResponse response = orderService.placeOrder(user, orderRequest);

        assertEquals("PENDING", response.getPaymentStatus());
    }

    // ── getMyOrders() ─────────────────────────────────────────────

    @Test
    @DisplayName("getMyOrders: should return list of orders for user")
    void getMyOrders_shouldReturnOrderList() {
        when(orderRepository.findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(order));
        when(orderItemRepository.findByOrder(order))
                .thenReturn(List.of(orderItem));

        List<OrderResponse> result = orderService.getMyOrders(user);

        assertEquals(1, result.size());
        assertEquals(301L, result.get(0).getId());
    }

    @Test
    @DisplayName("getMyOrders: user with no orders should return empty list")
    void getMyOrders_noOrders_shouldReturnEmptyList() {
        when(orderRepository.findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of());

        List<OrderResponse> result = orderService.getMyOrders(user);

        assertTrue(result.isEmpty());
    }

    // ── getOrderById() ────────────────────────────────────────────

    @Test
    @DisplayName("getOrderById: valid order belonging to user should return response")
    void getOrderById_validOrder_shouldReturnResponse() {
        when(orderRepository.findById(301L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrder(order)).thenReturn(List.of(orderItem));

        OrderResponse response = orderService.getOrderById(user, 301L);

        assertNotNull(response);
        assertEquals(301L, response.getId());
    }

    @Test
    @DisplayName("getOrderById: order not found should throw ResourceNotFoundException")
    void getOrderById_notFound_shouldThrowException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderById(user, 999L));
    }

    @Test
    @DisplayName("getOrderById: order belonging to another user should throw IllegalArgumentException")
    void getOrderById_unauthorizedUser_shouldThrowException() {
        User otherUser = User.builder()
                .name("Other").email("other@test.com")
                .password("pass").role(Role.USER).isVerified(true).build();
        ReflectionTestUtils.setField(otherUser, "id", 99L);

        when(orderRepository.findById(301L)).thenReturn(Optional.of(order));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.getOrderById(otherUser, 301L));

        assertTrue(ex.getMessage().contains("Unauthorized"));
    }

    // ── cancelOrder() ─────────────────────────────────────────────

    @Test
    @DisplayName("cancelOrder: PLACED order should be cancelled and stock restored")
    void cancelOrder_placedOrder_shouldCancelAndRestoreStock() {
        when(orderRepository.findById(301L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrder(order)).thenReturn(List.of(orderItem));
        when(orderRepository.save(order)).thenReturn(order);

        OrderResponse response = orderService.cancelOrder(user, 301L);

        assertEquals("CANCELLED", response.getStatus());
        verify(productService, times(1)).restoreStock(101L, 2);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("cancelOrder: non-PLACED order should throw IllegalArgumentException")
    void cancelOrder_nonPlacedOrder_shouldThrowException() {
        order.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(301L)).thenReturn(Optional.of(order));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.cancelOrder(user, 301L));

        assertTrue(ex.getMessage().contains("cannot be cancelled"));
        verify(productService, never()).restoreStock(any(), any());
    }

    @Test
    @DisplayName("cancelOrder: unauthorized user should throw IllegalArgumentException")
    void cancelOrder_unauthorizedUser_shouldThrowException() {
        User otherUser = User.builder()
                .name("Other").email("other@test.com")
                .password("pass").role(Role.USER).isVerified(true).build();
        ReflectionTestUtils.setField(otherUser, "id", 99L);

        when(orderRepository.findById(301L)).thenReturn(Optional.of(order));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.cancelOrder(otherUser, 301L));

        assertTrue(ex.getMessage().contains("Unauthorized"));
        verify(productService, never()).restoreStock(any(), any());
    }

    // ── updateOrderStatus() ───────────────────────────────────────

    @Test
    @DisplayName("updateOrderStatus: should update status successfully")
    void updateOrderStatus_shouldUpdateStatus() {
        OrderStatusUpdateRequest req = new OrderStatusUpdateRequest();
        req.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(301L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderItemRepository.findByOrder(order)).thenReturn(List.of(orderItem));

        OrderResponse response = orderService.updateOrderStatus(301L, req);

        assertEquals("SHIPPED", response.getStatus());
    }

    @Test
    @DisplayName("updateOrderStatus: CANCELLED order should throw IllegalArgumentException")
    void updateOrderStatus_cancelledOrder_shouldThrowException() {
        order.setStatus(OrderStatus.CANCELLED);
        OrderStatusUpdateRequest req = new OrderStatusUpdateRequest();
        req.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(301L)).thenReturn(Optional.of(order));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(301L, req));

        assertTrue(ex.getMessage().contains("Cannot update a cancelled order"));
        verify(orderRepository, never()).save(any());
    }

    // ── markAsPaid() ──────────────────────────────────────────────

    @Test
    @DisplayName("markAsPaid: should set status CONFIRMED and paymentStatus PAID")
    void markAsPaid_shouldSetPaidAndConfirmed() {
        when(orderRepository.findById(301L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.markAsPaid(301L);

        assertEquals(OrderStatus.CONFIRMED,    order.getStatus());
        assertEquals(PaymentStatus.PAID,       order.getPaymentStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("markAsPaid: order not found should throw ResourceNotFoundException")
    void markAsPaid_orderNotFound_shouldThrowException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.markAsPaid(999L));

        verify(orderRepository, never()).save(any());
    }

    // ── getOrderEntityById() ──────────────────────────────────────

    @Test
    @DisplayName("getOrderEntityById: should return Order entity")
    void getOrderEntityById_shouldReturnOrder() {
        when(orderRepository.findById(301L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderEntityById(301L);

        assertNotNull(result);
        assertEquals(301L, result.getId());
    }

    @Test
    @DisplayName("getOrderEntityById: not found should throw ResourceNotFoundException")
    void getOrderEntityById_notFound_shouldThrowException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderEntityById(999L));
    }
}
