package com.loadup.order.controller;

import com.loadup.order.api.OrderApi;
import com.loadup.order.dto.CancelOrderRequest;
import com.loadup.order.dto.CreateOrderRequest;
import com.loadup.order.dto.OrderResponse;
import com.loadup.order.dto.UpdateOrderRequest;
import com.loadup.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class OrderController implements OrderApi {
    private final Logger logger = Logger.getLogger(OrderApi.class.getName());
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String tenantId, @PathVariable String orderId, @RequestBody(required = false) CancelOrderRequest cancelOrderRequest) {
        logger.log(Level.INFO, "Received request to cancel order with tenantId: " + tenantId + " and orderId: " + orderId);
        OrderResponse orderResponse = orderService.cancelOrder(tenantId, orderId, cancelOrderRequest);
        return ResponseEntity.ok(orderResponse);
    }

    @Override
    public ResponseEntity<OrderResponse> createOrder(@PathVariable String tenantId, @RequestBody CreateOrderRequest createOrderRequest) {
        logger.log(Level.INFO, "Received request to create order with tenantId: " + tenantId);
        OrderResponse orderResponse = orderService.createOrder(tenantId, createOrderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @Override
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable String tenantId, @PathVariable String orderId, @RequestBody UpdateOrderRequest updateOrderRequest) {
        logger.log(Level.INFO, "Received request to update order with tenantId: " + tenantId + " and orderId: " + orderId);
        OrderResponse orderResponse = orderService.updateOrder(tenantId, orderId, updateOrderRequest);
        return ResponseEntity.ok(orderResponse);
    }
}
