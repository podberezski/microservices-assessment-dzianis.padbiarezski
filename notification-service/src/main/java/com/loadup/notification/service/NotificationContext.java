package com.loadup.notification.service;

import com.loadup.notification.dto.Customer;
import com.loadup.notification.messaging.OrderEventMessage;

public record NotificationContext(
        OrderEventMessage event,
        Customer customer,
        String notificationMessage
) {
}

