package com.loadup.notification.service;

import com.loadup.notification.dto.Customer;
import com.loadup.notification.dto.Order;
import com.loadup.notification.messaging.OrderEventMessage;
import com.loadup.notification.service.channel.NotificationChannel;
import com.loadup.notification.service.channel.NotificationChannelResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Handles the <b>order receipt</b> notification behavior.
 * <p>
 * A notification is sent to the customer whenever an order is
 * <em>created</em> or <em>updated</em>.
 */
@Slf4j
@Service
public class OrderReceiptNotificationService {

    private final NotificationChannelResolver channelResolver;

    public OrderReceiptNotificationService(NotificationChannelResolver channelResolver) {
        this.channelResolver = channelResolver;
    }

    public void notify(OrderEventMessage event, Order order) {
        String message = buildMessage(event);
        log.debug("Order receipt notification | tenantId={} orderId={} status={} message='{}'",
                event.tenantId(), event.orderId(), event.status(), message);

        NotificationContext context = new NotificationContext(event, order.getCustomer(), message);
        NotificationChannel channel = channelResolver.resolve(order.getCustomer().getCommunicationPreference());
        channel.send(context);
    }

    private String buildMessage(OrderEventMessage event) {
        return String.format(
                "Order receipt: Your order '%s' has been %s. Thank you for choosing us!",
                event.orderId(),
                event.status().toLowerCase()
        );
    }
}

