package com.loadup.notification.service;

import com.loadup.notification.dto.Order;
import com.loadup.notification.messaging.OrderEventMessage;
import com.loadup.notification.service.channel.NotificationChannel;
import com.loadup.notification.service.channel.NotificationChannelResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Handles the <b>completion acknowledgement</b> notification behavior.
 * <p>
 * A notification is sent to the customer when an order reaches a
 * <em>completed</em> or other <em>terminal</em> state
 * ({@code COMPLETED}, {@code CANCELLED}, {@code FAILED}).
 */
@Slf4j
@Service
public class CompletionAcknowledgementNotificationService {

    private final NotificationChannelResolver channelResolver;

    public CompletionAcknowledgementNotificationService(NotificationChannelResolver channelResolver) {
        this.channelResolver = channelResolver;
    }

    public void notify(OrderEventMessage event, Order order) {
        String message = buildMessage(event);
        log.debug("Completion acknowledgement notification | tenantId={} orderId={} status={} message='{}'",
                event.tenantId(), event.orderId(), event.status(), message);

        NotificationContext context = new NotificationContext(event, order.getCustomer(), message);
        NotificationChannel channel = channelResolver.resolve(order.getCustomer().getCommunicationPreference());
        channel.send(context);
    }

    private String buildMessage(OrderEventMessage event) {
        return String.format(
                "Completion acknowledgement: Your order '%s' has reached a final state: %s.",
                event.orderId(),
                event.status()
        );
    }
}

