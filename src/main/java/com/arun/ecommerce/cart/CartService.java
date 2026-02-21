package com.arun.ecommerce.cart;

import com.arun.ecommerce.cart.dto.CartItemResponse;
import com.arun.ecommerce.cart.dto.CartRequest;
import com.arun.ecommerce.entity.CartItem;
import com.arun.ecommerce.entity.User;

import java.util.List;

public interface CartService {
    List<CartItemResponse> getCart(User user);
    CartItemResponse       addItem(User user, CartRequest request);
    CartItemResponse       updateItem(User user, CartRequest request);
    void                   removeItem(User user, Long cartItemId);
    void                   clearCart(User user);
    List<CartItem> getCartItemEntities(User user);
}
