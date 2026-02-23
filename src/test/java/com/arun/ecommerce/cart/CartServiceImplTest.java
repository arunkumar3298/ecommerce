package com.arun.ecommerce.cart;

import com.arun.ecommerce.cart.dto.CartItemResponse;
import com.arun.ecommerce.cart.dto.CartRequest;
import com.arun.ecommerce.entity.CartItem;
import com.arun.ecommerce.entity.Product;
import com.arun.ecommerce.entity.User;
import com.arun.ecommerce.entity.enums.Role;
import com.arun.ecommerce.exception.ResourceNotFoundException;
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
class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private ProductService productService;

    @InjectMocks private CartServiceImpl cartService;

    private User    user;
    private Product product;
    private CartItem cartItem;
    private CartRequest cartRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("Arun Kumar")
                .email("arun@test.com")
                .password("encoded")
                .role(Role.USER)
                .isVerified(true)
                .build();
        // Inject id=1L via ReflectionTestUtils (no setter on BaseEntity)
        ReflectionTestUtils.setField(user, "id", 1L);

        product = Product.builder()
                .name("iPhone 15")
                .description("Apple smartphone")
                .price(new BigDecimal("79999.00"))
                .stock(10)
                .category("Electronics")
                .imageUrl("iphone.jpg")
                .build();
        ReflectionTestUtils.setField(product, "id", 101L);

        cartItem = CartItem.builder()
                .user(user)
                .product(product)
                .quantity(2)
                .build();
        ReflectionTestUtils.setField(cartItem, "id", 201L);

        cartRequest = new CartRequest();
        cartRequest.setProductId(101L);
        cartRequest.setQuantity(2);
    }

    // ── getCart() ─────────────────────────────────────────────────

    @Test
    @DisplayName("getCart: should return list of CartItemResponse for user")
    void getCart_shouldReturnCartItems() {
        when(cartRepository.findByUser(user)).thenReturn(List.of(cartItem));

        List<CartItemResponse> result = cartService.getCart(user);

        assertEquals(1, result.size());
        assertEquals(101L,        result.get(0).getProductId());
        assertEquals("iPhone 15", result.get(0).getProductName());
        assertEquals(2,           result.get(0).getQuantity());
    }

    @Test
    @DisplayName("getCart: empty cart should return empty list")
    void getCart_emptyCart_shouldReturnEmptyList() {
        when(cartRepository.findByUser(user)).thenReturn(List.of());

        List<CartItemResponse> result = cartService.getCart(user);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getCart: subtotal should be price × quantity")
    void getCart_subtotalShouldBeCorrect() {
        when(cartRepository.findByUser(user)).thenReturn(List.of(cartItem));

        List<CartItemResponse> result = cartService.getCart(user);

        // 79999.00 × 2 = 159998.00
        BigDecimal expected = new BigDecimal("159998.00");
        assertEquals(0, expected.compareTo(result.get(0).getSubtotal()));
    }

    // ── addItem() ─────────────────────────────────────────────────

    @Test
    @DisplayName("addItem: new item should be saved to cart")
    void addItem_newItem_shouldSaveToCart() {
        when(productService.getProductEntityById(101L)).thenReturn(product);
        when(cartRepository.findByUserAndProduct(user, product))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(CartItem.class))).thenReturn(cartItem);

        CartItemResponse response = cartService.addItem(user, cartRequest);

        assertNotNull(response);
        verify(cartRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("addItem: existing item should update quantity instead of duplicate")
    void addItem_existingItem_shouldUpdateQuantity() {
        CartItem existingItem = CartItem.builder()
                .user(user).product(product).quantity(1).build();
        ReflectionTestUtils.setField(existingItem, "id", 201L);

        when(productService.getProductEntityById(101L)).thenReturn(product);
        when(cartRepository.findByUserAndProduct(user, product))
                .thenReturn(Optional.of(existingItem));
        when(cartRepository.save(existingItem)).thenReturn(existingItem);

        cartService.addItem(user, cartRequest); // adding 2 more

        // quantity should be updated to 1 + 2 = 3
        assertEquals(3, existingItem.getQuantity());
        verify(cartRepository, times(1)).save(existingItem);
    }

    @Test
    @DisplayName("addItem: insufficient stock should throw RuntimeException")
    void addItem_insufficientStock_shouldThrowException() {
        product.setStock(1); // only 1 in stock
        cartRequest.setQuantity(5); // requesting 5

        when(productService.getProductEntityById(101L)).thenReturn(product);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.addItem(user, cartRequest));

        assertTrue(ex.getMessage().contains("Insufficient stock"));
        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("addItem: product not found should throw ResourceNotFoundException")
    void addItem_productNotFound_shouldThrowException() {
        when(productService.getProductEntityById(101L))
                .thenThrow(new ResourceNotFoundException("Product not found with id: 101"));

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.addItem(user, cartRequest));

        verify(cartRepository, never()).save(any());
    }

    // ── updateItem() ──────────────────────────────────────────────

    @Test
    @DisplayName("updateItem: should update quantity for existing cart item")
    void updateItem_shouldUpdateQuantity() {
        cartRequest.setQuantity(5);

        when(productService.getProductEntityById(101L)).thenReturn(product);
        when(cartRepository.findByUserAndProduct(user, product))
                .thenReturn(Optional.of(cartItem));
        when(cartRepository.save(cartItem)).thenReturn(cartItem);

        CartItemResponse response = cartService.updateItem(user, cartRequest);

        assertNotNull(response);
        assertEquals(5, cartItem.getQuantity());
        verify(cartRepository, times(1)).save(cartItem);
    }

    @Test
    @DisplayName("updateItem: item not in cart should throw ResourceNotFoundException")
    void updateItem_itemNotInCart_shouldThrowException() {
        when(productService.getProductEntityById(101L)).thenReturn(product);
        when(cartRepository.findByUserAndProduct(user, product))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.updateItem(user, cartRequest));

        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateItem: insufficient stock should throw RuntimeException")
    void updateItem_insufficientStock_shouldThrowException() {
        product.setStock(1);
        cartRequest.setQuantity(5);

        when(productService.getProductEntityById(101L)).thenReturn(product);
        when(cartRepository.findByUserAndProduct(user, product))
                .thenReturn(Optional.of(cartItem));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.updateItem(user, cartRequest));

        assertTrue(ex.getMessage().contains("Insufficient stock"));
        verify(cartRepository, never()).save(any());
    }

    // ── removeItem() ──────────────────────────────────────────────

    @Test
    @DisplayName("removeItem: valid item belonging to user should be deleted")
    void removeItem_validItem_shouldDelete() {
        when(cartRepository.findById(201L)).thenReturn(Optional.of(cartItem));

        cartService.removeItem(user, 201L);

        verify(cartRepository, times(1)).deleteById(201L);
    }

    @Test
    @DisplayName("removeItem: cart item not found should throw ResourceNotFoundException")
    void removeItem_itemNotFound_shouldThrowException() {
        when(cartRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.removeItem(user, 999L));

        verify(cartRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("removeItem: item belonging to another user should throw RuntimeException")
    void removeItem_unauthorizedUser_shouldThrowException() {
        User otherUser = User.builder()
                .name("Other").email("other@test.com")
                .password("pass").role(Role.USER).isVerified(true).build();
        ReflectionTestUtils.setField(otherUser, "id", 99L);

        // cartItem belongs to user (id=1), but otherUser (id=99) tries to remove
        when(cartRepository.findById(201L)).thenReturn(Optional.of(cartItem));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.removeItem(otherUser, 201L));

        assertTrue(ex.getMessage().contains("Unauthorized"));
        verify(cartRepository, never()).deleteById(any());
    }

    // ── clearCart() ───────────────────────────────────────────────

    @Test
    @DisplayName("clearCart: should delete all items for user")
    void clearCart_shouldDeleteAllItems() {
        cartService.clearCart(user);

        verify(cartRepository, times(1)).deleteByUser(user);
    }

    // ── getCartItemEntities() ─────────────────────────────────────

    @Test
    @DisplayName("getCartItemEntities: should return raw CartItem list")
    void getCartItemEntities_shouldReturnRawList() {
        when(cartRepository.findByUser(user)).thenReturn(List.of(cartItem));

        List<CartItem> result = cartService.getCartItemEntities(user);

        assertEquals(1, result.size());
        assertEquals(cartItem, result.get(0));
    }
}
