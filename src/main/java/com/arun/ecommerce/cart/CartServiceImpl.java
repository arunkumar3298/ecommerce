package com.arun.ecommerce.cart;

import com.arun.ecommerce.cart.dto.CartItemResponse;
import com.arun.ecommerce.cart.dto.CartRequest;
import com.arun.ecommerce.entity.CartItem;
import com.arun.ecommerce.entity.Product;
import com.arun.ecommerce.entity.User;
import com.arun.ecommerce.exception.ResourceNotFoundException;
import com.arun.ecommerce.product.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductService productService; // interface — NOT ProductRepository ✅

    public CartServiceImpl(CartRepository cartRepository,
                           ProductService productService) {
        this.cartRepository = cartRepository;
        this.productService = productService;
    }

    // ── Get Cart ──────────────────────────────────────────────

    @Override
    public List<CartItemResponse> getCart(User user) {
        return cartRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Add Item ──────────────────────────────────────────────

    @Override
    @Transactional
    public CartItemResponse addItem(User user, CartRequest request) {
        Product product = productService.getProductEntityById(request.getProductId());

        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStock());
        }

        // If item already in cart → update quantity instead of duplicate insert
        Optional<CartItem> existing = cartRepository.findByUserAndProduct(user, product);

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            return toResponse(cartRepository.save(item));
        }

        // New cart item
        CartItem newItem = CartItem.builder()
                .user(user)
                .product(product)
                .quantity(request.getQuantity())
                .build();

        return toResponse(cartRepository.save(newItem));
    }

    // ── Update Quantity ───────────────────────────────────────

    @Override
    @Transactional
    public CartItemResponse updateItem(User user, CartRequest request) {
        Product product = productService.getProductEntityById(request.getProductId());

        CartItem item = cartRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item not in cart. Add it first."));

        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStock());
        }

        item.setQuantity(request.getQuantity());
        return toResponse(cartRepository.save(item));
    }

    // ── Remove Single Item ────────────────────────────────────

    @Override
    @Transactional
    public void removeItem(User user, Long cartItemId) {
        CartItem item = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found with id: " + cartItemId));

        // Security: ensure the item belongs to THIS user — not someone else's cart
        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: This item does not belong to your cart");
        }

        cartRepository.deleteById(cartItemId);
    }

    // ── Clear Entire Cart ─────────────────────────────────────

    @Override
    @Transactional
    public void clearCart(User user) {
        cartRepository.deleteByUser(user);
    }

    // ── Private Mapper (Entity → DTO) ─────────────────────────

    private CartItemResponse toResponse(CartItem item) {
        BigDecimal subtotal = item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
                .cartItemId(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productCategory(item.getProduct().getCategory())
                .productImageUrl(item.getProduct().getImageUrl())
                .productPrice(item.getProduct().getPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
    }
    @Override
    public List<CartItem> getCartItemEntities(User user) {
        return cartRepository.findByUser(user);
    }

}
