package com.loadup.order.service;

import com.loadup.order.cache.OrderCacheService;
import com.loadup.order.dto.CommunicationPreference;
import com.loadup.order.dto.CreateOrderRequest;
import com.loadup.order.dto.Customer;
import com.loadup.order.mapping.OrderMapping;
import com.loadup.order.messaging.OrderKafkaPublisher;
import com.loadup.order.repository.OrderRepository;
import com.loadup.order.repository.model.OrderEntity;
import com.loadup.order.repository.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OrderServiceCacheTest {

    @Test
    void shouldCacheOrderAfterCreatePersistsInDatabase() {
        OrderRepository repository = mock(OrderRepository.class);
        OrderMapping mapping = mock(OrderMapping.class);
        OrderCacheService cacheService = mock(OrderCacheService.class);
        OrderKafkaPublisher orderKafkaPublisher = mock(OrderKafkaPublisher.class);
        TenantService tenantService = mock(TenantService.class);
        OrderService service = new OrderService(repository, mapping, cacheService, orderKafkaPublisher, tenantService);

        String tenantId = "tenant-1";
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomer(new Customer()
                .customerId("customer-1")
                .communicationPreference(CommunicationPreference.EMAIL)
                .email("customer-1@loadup.com"));
        request.setCurrency("USD");
        request.setItems(List.of());

        OrderEntity toSave = OrderEntity.builder()
                .tenantId(tenantId)
                .orderId("order-1")
                .customerId("customer-1")
                .status(OrderStatus.CREATED)
                .currency("USD")
                .totalAmount(0.0)
                .items(List.of())
                .build();

        OrderEntity saved = OrderEntity.builder()
                .tenantId(tenantId)
                .orderId("order-1")
                .customerId("customer-1")
                .status(OrderStatus.CREATED)
                .currency("USD")
                .totalAmount(0.0)
                .items(List.of())
                .build();

        when(mapping.toOrderEntity(eq(request), eq(tenantId), anyString())).thenReturn(toSave);
        when(repository.save(toSave)).thenReturn(saved);

        service.createOrder(tenantId, request);

        verify(tenantService).initiateTenantContext(tenantId);
        verify(repository).save(toSave);
        verify(cacheService).cacheOrder(saved);
        verify(orderKafkaPublisher).sendOrderStatusEvent(saved);
    }

    @Test
    void shouldRejectCreateOrderWhenEmailPreferenceHasNoEmail() {
        OrderRepository repository = mock(OrderRepository.class);
        OrderMapping mapping = mock(OrderMapping.class);
        OrderCacheService cacheService = mock(OrderCacheService.class);
        OrderKafkaPublisher orderKafkaPublisher = mock(OrderKafkaPublisher.class);
        TenantService tenantService = mock(TenantService.class);
        OrderService service = new OrderService(repository, mapping, cacheService, orderKafkaPublisher, tenantService);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomer(new Customer().communicationPreference(CommunicationPreference.EMAIL));
        request.setCurrency("USD");
        request.setItems(List.of());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createOrder("tenant-1", request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("customer.email is required when communicationPreference is EMAIL", exception.getReason());
        verifyNoInteractions(tenantService);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectCreateOrderWhenMailPreferenceHasNoAddress() {
        OrderRepository repository = mock(OrderRepository.class);
        OrderMapping mapping = mock(OrderMapping.class);
        OrderCacheService cacheService = mock(OrderCacheService.class);
        OrderKafkaPublisher orderKafkaPublisher = mock(OrderKafkaPublisher.class);
        TenantService tenantService = mock(TenantService.class);
        OrderService service = new OrderService(repository, mapping, cacheService, orderKafkaPublisher, tenantService);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomer(new Customer().communicationPreference(CommunicationPreference.MAIL));
        request.setCurrency("USD");
        request.setItems(List.of());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createOrder("tenant-1", request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("customer.address is required when communicationPreference is MAIL", exception.getReason());
        verifyNoInteractions(tenantService);
        verifyNoInteractions(repository);
    }
}



