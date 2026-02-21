package com.arun.ecommerce.cart;

import com.arun.ecommerce.cart.dto.CartItemResponse;
import com.arun.ecommerce.cart.dto.CartRequest;
import com.arun.ecommerce.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCart(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(cartService.getCart(currentUser));
    }

    @PostMapping("/add")
    public ResponseEntity<CartItemResponse> addItem(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CartRequest request) {
        return ResponseEntity.ok(cartService.addItem(currentUser, request));
    }

    @PutMapping("/update")
    public ResponseEntity<CartItemResponse> updateItem(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CartRequest request) {
        return ResponseEntity.ok(cartService.updateItem(currentUser, request));
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<String> removeItem(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long cartItemId) {
        cartService.removeItem(currentUser, cartItemId);
        return ResponseEntity.ok("Item removed from cart");
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(
            @AuthenticationPrincipal User currentUser) {
        cartService.clearCart(currentUser);
        return ResponseEntity.ok("Cart cleared successfully");
    }
}
