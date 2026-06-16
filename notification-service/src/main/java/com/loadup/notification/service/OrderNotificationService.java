package com.loadup.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loadup.notification.cache.OrderRedisCacheService;
import com.loadup.notification.dto.Customer;
import com.loadup.notification.dto.Order;
import com.loadup.notification.messaging.OrderEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Orchestrates order event processing and dispatches the appropriate
 * notification behavior based on the event's {@link OrderStatus}.
 *
 * <ul>
 *   <li><b>Order receipt</b> – fired on {@code CREATED} or {@code UPDATED}</li>
 *   <li><b>Completion acknowledgement</b> – fired on terminal states
 *       ({@code COMPLETED}, {@code CANCELLED}, {@code FAILED})</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderNotificationService {

    private final OrderRedisCacheService cacheService;
    private final OrderReceiptNotificationService receiptNotificationService;
    private final CompletionAcknowledgementNotificationService completionNotificationService;
    private final ObjectMapper objectMapper;


    public void process(OrderEventMessage event) {
        OrderStatus status = parseStatus(event);
        if (status == null) {
            log.warn("Unknown order status '{}' – skipping notification. orderId={} tenantId={}",
                    event.status(), event.orderId(), event.tenantId());
            return;
        }

        Order order = resolveOrder(event);
        if (order == null) {
            log.warn("Customer not found for orderId={} tenantId={} – skipping notification",
                    event.orderId(), event.tenantId());
            return;
        }

        if (status.isReceiptTrigger()) {
            log.info("Triggering order receipt notification | orderId={} status={}", event.orderId(), event.status());
            receiptNotificationService.notify(event, order);

        } else if (status.isTerminalState()) {
            log.info("Triggering completion acknowledgement notification | orderId={} status={}", event.orderId(), event.status());
            completionNotificationService.notify(event, order);

        } else {
            log.debug("No notification configured for status '{}' | orderId={}", event.status(), event.orderId());
        }
    }

    private OrderStatus parseStatus(OrderEventMessage event) {
        try {
            return OrderStatus.valueOf(event.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    private Order resolveOrder(OrderEventMessage event) {
        return cacheService.getOrder(event.tenantId(), event.orderId())
                .map(json -> deserializeOrder(json, event))
                .orElseGet(() -> {
                    log.debug("Order not found in cache, using fallback to db stub | orderId={}", event.orderId());
                    return retrieveFromDb(event);
                });
    }

    private Order deserializeOrder(String json, OrderEventMessage event) {
        try {
            return objectMapper.readValue(json, Order.class);
        } catch (Exception e) {
            log.error("Failed to deserialize cached order JSON | orderId={} tenantId={}",
                    event.orderId(), event.tenantId(), e);
            return null;
        }
    }


    private Order retrieveFromDb(OrderEventMessage event) {
        Order order = new Order();
        Customer customer = new Customer();
        customer.setCommunicationPreference(Customer.CommunicationPreference.EMAIL);
        customer.setEmail("some@loadup.com");
        order.setCustomer(customer);
        return order;
    }
}

