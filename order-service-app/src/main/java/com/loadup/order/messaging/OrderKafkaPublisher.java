package com.loadup.order.messaging;

import com.loadup.order.config.OrderMessagingProperties;
import com.loadup.order.repository.model.OrderEntity;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class OrderKafkaPublisher {

    private static final String TRACE_ID_HEADER = "traceId";
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderKafkaPublisher.class);

    private final StreamBridge streamBridge;
    private final OrderMessagingProperties orderMessagingProperties;
    private final Tracer tracer;

    @Async
    public void sendOrderStatusEvent(OrderEntity orderEntity) {
        OrderEventMessage event = OrderEventMessage.builder()
                .tenantId(orderEntity.getTenantId())
                .orderId(orderEntity.getOrderId())
                .status(orderEntity.getStatus().name())
                .build();

        String partitionKey = buildPartitionKey(orderEntity);
        String traceId = resolveCurrentTraceId();

        MessageBuilder<OrderEventMessage> messageBuilder = MessageBuilder.withPayload(event)
                .setHeader(KafkaHeaders.KEY, partitionKey.getBytes(StandardCharsets.UTF_8));
        if (traceId != null) {
            messageBuilder.setHeader(TRACE_ID_HEADER, traceId);
        }

        Message<OrderEventMessage> message = messageBuilder.build();

        boolean sent = false;
        try {
            sent = streamBridge.send(orderMessagingProperties.getBindingName(), message);
        } catch (Exception e) {
            LOGGER.error("Failed to publish order event for tenantId={}, orderId={}. Error: {}",
                    event.tenantId(), event.orderId(), e.getMessage(), e);
        }
        if (!sent) {
            throw new IllegalStateException("Failed to publish order event to Kafka binding "
                    + orderMessagingProperties.getBindingName());
        }

        LOGGER.info("Order event published for tenantId={}, orderId={}, status={}",
                event.tenantId(), event.orderId(), event.status());
    }

    private String resolveCurrentTraceId() {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan == null) {
            return null;
        }
        return currentSpan.context().traceId();
    }

    private String buildPartitionKey(OrderEntity orderEntity) {
        return orderEntity.getTenantId() + "|" + orderEntity.getOrderId();
    }
}

