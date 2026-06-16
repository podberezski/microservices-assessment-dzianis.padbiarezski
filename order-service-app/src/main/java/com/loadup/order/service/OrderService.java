package com.loadup.order.service;

import com.loadup.order.cache.OrderCacheService;
import com.loadup.order.dto.CancelOrderRequest;
import com.loadup.order.dto.CommunicationPreference;
import com.loadup.order.dto.Customer;
import com.loadup.order.dto.CreateOrderRequest;
import com.loadup.order.dto.OrderResponse;
import com.loadup.order.dto.UpdateOrderRequest;
import com.loadup.order.mapping.OrderMapping;
import com.loadup.order.messaging.OrderKafkaPublisher;
import com.loadup.order.repository.OrderRepository;
import com.loadup.order.repository.model.OrderEntity;
import com.loadup.order.repository.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final EnumSet<OrderStatus> CANCELLABLE_STATUSES = EnumSet.of(
            OrderStatus.CREATED,
            OrderStatus.CONFIRMED,
            OrderStatus.PAID
    );

    private final OrderRepository orderRepository;
    private final OrderMapping orderMapping;
    private final OrderCacheService redisOrderCacheService;
    private final OrderKafkaPublisher orderKafkaPublisher;
    private final TenantService tenantService;

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public OrderResponse createOrder(String tenantId, CreateOrderRequest request) {
        validateCustomerContactPreference(request);

        tenantService.initiateTenantContext(tenantId);

        String orderId = UUID.randomUUID().toString();
        OrderEntity entity = orderMapping.toOrderEntity(request, tenantId, orderId);
        entity.setStatus(OrderStatus.CREATED);
        OrderEntity saved = orderRepository.save(entity);
        persistToCacheAndPublish(saved);
        return orderMapping.toOrderResponse(saved);
    }

    private void validateCustomerContactPreference(CreateOrderRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "createOrder request is required");
        }

        Customer customer = request.getCustomer();
        if (customer == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customer is required");
        }

        CommunicationPreference preference = customer.getCommunicationPreference();
        if (preference == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customer.communicationPreference is required");
        }

        if (preference == CommunicationPreference.EMAIL && isBlank(customer.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "customer.email is required when communicationPreference is EMAIL"
            );
        }

        if (preference == CommunicationPreference.MAIL && isBlank(customer.getAddress())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "customer.address is required when communicationPreference is MAIL"
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public OrderResponse updateOrder(String tenantId, String orderId, UpdateOrderRequest request) {
        OrderEntity existing = findRequired(tenantId, orderId);

        OrderStatus targetStatus = orderMapping.toOrderStatus(request.getTargetStatus());
        validateUpdateTransition(existing.getStatus(), targetStatus);

        orderMapping.updateOrderEntity(request, existing);
        OrderEntity saved = orderRepository.save(existing);
        persistToCacheAndPublish(saved);
        return orderMapping.toOrderResponse(saved);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public OrderResponse cancelOrder(String tenantId, String orderId, CancelOrderRequest request) {
        OrderEntity existing = findRequired(tenantId, orderId);

        if (!CANCELLABLE_STATUSES.contains(existing.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order cannot be cancelled from status " + existing.getStatus()
            );
        }

        existing.setStatus(OrderStatus.CANCELLED);
        OrderEntity saved = orderRepository.save(existing);
        persistToCacheAndPublish(saved);
        return orderMapping.toOrderResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderEntity getOrderEntity(String tenantId, String orderId) {
        return findRequired(tenantId, orderId);
    }

    private OrderEntity findRequired(String tenantId, String orderId) {
        return orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found for tenantId=" + tenantId + ", orderId=" + orderId
                ));
    }

    private void persistToCacheAndPublish(OrderEntity saved) {
        redisOrderCacheService.cacheOrder(saved);
        orderKafkaPublisher.sendOrderStatusEvent(saved);
    }

    private void validateUpdateTransition(OrderStatus currentStatus, OrderStatus targetStatus) {
        if (targetStatus == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "targetStatus is required");
        }
        if (currentStatus == OrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cancelled order cannot be updated");
        }
        if (targetStatus == OrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Use cancel operation to cancel an order");
        }
        if (targetStatus.ordinal() < currentStatus.ordinal()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Backward status transition is not allowed: " + currentStatus + " -> " + targetStatus
            );
        }
    }

}

