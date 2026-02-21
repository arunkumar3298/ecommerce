package com.arun.ecommerce.order;

import com.arun.ecommerce.entity.Order;
import com.arun.ecommerce.entity.User;
import com.arun.ecommerce.order.dto.OrderRequest;
import com.arun.ecommerce.order.dto.OrderResponse;
import com.arun.ecommerce.order.dto.OrderStatusUpdateRequest;

import java.util.List;

public interface OrderService {

    OrderResponse       placeOrder(User user, OrderRequest request);

    List<OrderResponse> getMyOrders(User user);

    OrderResponse       getOrderById(User user, Long orderId);

    List<OrderResponse> getAllOrders();

    OrderResponse       updateOrderStatus(Long orderId,
                                          OrderStatusUpdateRequest request);

    OrderResponse       cancelOrder(User user, Long orderId);

    Order               getOrderEntityById(Long orderId);

    void                markAsPaid(Long orderId);
}
