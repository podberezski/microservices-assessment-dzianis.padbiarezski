package com.loadup.order.messaging;

import com.loadup.order.config.OrderMessagingProperties;
import com.loadup.order.repository.model.OrderEntity;
import com.loadup.order.repository.model.OrderStatus;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

class OrderKafkaPublisherTest {

    @Test
    void shouldSendKafkaMessageWithTenantAndOrderBasedMessageKey() {
        StreamBridge streamBridge = mock(StreamBridge.class);
        Tracer tracer = mock(Tracer.class);
        Span span = mock(Span.class);
        TraceContext traceContext = mock(TraceContext.class);
        OrderMessagingProperties properties = new OrderMessagingProperties();
        properties.setBindingName("orderEvents-out-0");

        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn("trace-abc-123");

        OrderKafkaPublisher publisher = new OrderKafkaPublisher(streamBridge, properties, tracer);
        OrderEntity orderEntity = OrderEntity.builder()
                .tenantId("tenant-1")
                .orderId("order-42")
                .status(OrderStatus.CONFIRMED)
                .items(List.of())
                .build();

        when(streamBridge.send(eq("orderEvents-out-0"), org.mockito.ArgumentMatchers.any(Message.class)))
                .thenReturn(true);

        publisher.sendOrderStatusEvent(orderEntity);

        ArgumentCaptor<Message<?>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(streamBridge).send(eq("orderEvents-out-0"), messageCaptor.capture());

        Message<?> sentMessage = messageCaptor.getValue();
        assertInstanceOf(OrderEventMessage.class, sentMessage.getPayload());
        OrderEventMessage payload = (OrderEventMessage) sentMessage.getPayload();
        assertEquals("tenant-1", payload.tenantId());
        assertEquals("order-42", payload.orderId());
        assertEquals("CONFIRMED", payload.status());

        byte[] messageKey = (byte[]) sentMessage.getHeaders().get(KafkaHeaders.KEY);
        assertArrayEquals("tenant-1|order-42".getBytes(StandardCharsets.UTF_8), messageKey);
        assertEquals("trace-abc-123", sentMessage.getHeaders().get("traceId"));
    }
}


