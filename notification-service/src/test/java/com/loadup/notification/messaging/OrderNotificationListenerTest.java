package com.loadup.notification.messaging;

import com.loadup.notification.service.OrderNotificationService;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OrderNotificationListenerTest {

    @Test
    void shouldPopulateMdcTraceIdFromMessageHeader() {
        OrderNotificationService orderNotificationService = mock(OrderNotificationService.class);
        OrderNotificationListener listener = new OrderNotificationListener(orderNotificationService);

        doAnswer(invocation -> {
            assertEquals("trace-1", MDC.get("traceId"));
            return null;
        }).when(orderNotificationService).process(any(OrderEventMessage.class));

        Acknowledgment acknowledgment = mock(Acknowledgment.class);
        Message<OrderEventMessage> message = MessageBuilder.withPayload(OrderEventMessage.builder()
                        .tenantId("tenant-1")
                        .orderId("order-42")
                        .status("CONFIRMED")
                        .build())
                .setHeader("traceId", "trace-1")
                .setHeader(KafkaHeaders.ACKNOWLEDGMENT, acknowledgment)
                .build();

        Consumer<Message<OrderEventMessage>> consumer = listener.orderUpdateConsumer();
        consumer.accept(message);

        verify(acknowledgment).acknowledge();
        assertNull(MDC.get("traceId"));
    }
}

