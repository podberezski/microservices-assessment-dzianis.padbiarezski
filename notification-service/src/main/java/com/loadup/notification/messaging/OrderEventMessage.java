package com.loadup.notification.messaging;

import lombok.Builder;

@Builder
public record OrderEventMessage(
        String tenantId,
        String orderId,
        String status
) {
}

