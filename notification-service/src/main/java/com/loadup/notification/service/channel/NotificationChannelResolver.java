package com.loadup.notification.service.channel;

import com.loadup.notification.dto.Customer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves the appropriate {@link NotificationChannel} strategy based on the
 * customer's {@link Customer.CommunicationPreference}.
 */
@Component
public class NotificationChannelResolver {

    private final Map<Customer.CommunicationPreference, NotificationChannel> channelsByPreference;

    public NotificationChannelResolver(List<NotificationChannel> channels) {
        this.channelsByPreference = channels.stream()
                .collect(Collectors.toMap(NotificationChannel::getSupportedPreference, Function.identity()));
    }

    /**
     * Returns the channel matching the customer's communication preference.
     *
     * @param preference the customer's preferred communication channel
     * @return the matching {@link NotificationChannel}
     * @throws IllegalArgumentException if no channel is registered for the given preference
     */
    public NotificationChannel resolve(Customer.CommunicationPreference preference) {
        NotificationChannel channel = channelsByPreference.get(preference);
        if (channel == null) {
            throw new IllegalArgumentException(
                    "No notification channel registered for preference: " + preference);
        }
        return channel;
    }
}

