package com.loadup.notification.messaging;

import com.loadup.notification.service.OrderNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Slf4j
@Component
public class OrderNotificationListener {

    private static final String TRACE_ID_HEADER = "traceId";

    private final OrderNotificationService orderNotificationService;

    public OrderNotificationListener(OrderNotificationService orderNotificationService) {
        this.orderNotificationService = orderNotificationService;
    }

    @Bean
    public Consumer<Message<OrderEventMessage>> orderUpdateConsumer() {
        return message -> {
            String previousTraceId = useMessageTraceId(message);

            try {
                OrderEventMessage orderEvent = message.getPayload();
                log.info("Received order event: {}", orderEvent);

                orderNotificationService.process(orderEvent);

                Acknowledgment acknowledgment = message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                    log.info("Order event acknowledged.");
                } else {
                    log.warn("No acknowledgment header present; event was processed without manual ack");
                }
            } catch (Exception e) {
                log.error("Error processing order event", e);
                throw new RuntimeException("Failed to process order event", e);
            } finally {
                restorePreviousTraceId(previousTraceId);
            }
        };
    }

    // TODO: This should be moved to some more elegant approach, but do not have enought time
    private String useMessageTraceId(Message<OrderEventMessage> message) {
        String previousTraceId = MDC.get(TRACE_ID_HEADER);
        String traceId = extractTraceId(message);
        if (traceId != null) {
            MDC.put(TRACE_ID_HEADER, traceId);
        }
        return previousTraceId;
    }

    private String extractTraceId(Message<OrderEventMessage> message) {
        Object traceHeader = message.getHeaders().get(TRACE_ID_HEADER);
        if (traceHeader instanceof String traceId && !traceId.isBlank()) {
            return traceId;
        }
        if (traceHeader instanceof byte[] traceIdBytes) {
            String decodedTraceId = new String(traceIdBytes, StandardCharsets.UTF_8);
            if (!decodedTraceId.isBlank()) {
                return decodedTraceId;
            }
        }
        return null;
    }

    private void restorePreviousTraceId(String previousTraceId) {
        if (previousTraceId == null || previousTraceId.isBlank()) {
            MDC.remove(TRACE_ID_HEADER);
            return;
        }
        MDC.put(TRACE_ID_HEADER, previousTraceId);
    }
}
