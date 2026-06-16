package com.loadup.notification.service.channel;

import com.loadup.notification.dto.Customer;
import com.loadup.notification.service.NotificationContext;

/**
 * Strategy interface for sending a customer notification via a specific channel.
 */
public interface NotificationChannel {

    /**
     * Sends the notification described in the given context.
     *
     * @param context all data required to compose and dispatch the notification
     */
    void send(NotificationContext context);

    /**
     * Returns the communication preference this channel handles.
     */
    Customer.CommunicationPreference getSupportedPreference();
}

