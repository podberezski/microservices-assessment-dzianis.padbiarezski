package com.loadup.notification.service.channel;

import com.loadup.notification.dto.Customer;
import com.loadup.notification.service.NotificationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MailNotificationChannel implements NotificationChannel {

    @Override
    public void send(NotificationContext context) {
        String address = context.customer().getAddress();
        String orderId = context.event().orderId();
        String tenantId = context.event().tenantId();
        String message = context.notificationMessage();

        log.info("[MAIL] Sending notification to customer address='{}' | tenantId={} | orderId={} | message='{}'",
                address, tenantId, orderId, message);
    }

    @Override
    public Customer.CommunicationPreference getSupportedPreference() {
        return Customer.CommunicationPreference.MAIL;
    }
}

