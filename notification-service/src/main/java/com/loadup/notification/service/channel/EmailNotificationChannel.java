package com.loadup.notification.service.channel;

import com.loadup.notification.dto.Customer;
import com.loadup.notification.service.NotificationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailNotificationChannel implements NotificationChannel {

    @Override
    public void send(NotificationContext context) {
        String email = context.customer().getEmail();
        String orderId = context.event().orderId();
        String tenantId = context.event().tenantId();
        String message = context.notificationMessage();

        log.info("[EMAIL] Sending notification to customer email={} | tenantId={} | orderId={} | message='{}'",
                email, tenantId, orderId, message);
    }

    @Override
    public Customer.CommunicationPreference getSupportedPreference() {
        return Customer.CommunicationPreference.EMAIL;
    }
}

