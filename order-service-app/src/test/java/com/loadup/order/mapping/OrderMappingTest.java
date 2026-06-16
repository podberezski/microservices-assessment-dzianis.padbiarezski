package com.loadup.order.mapping;

import com.loadup.order.dto.CreateOrderRequest;
import com.loadup.order.dto.Customer;
import com.loadup.order.dto.CommunicationPreference;
import com.loadup.order.dto.OrderItem;
import com.loadup.order.dto.UpdateOrderRequest;
import com.loadup.order.repository.model.OrderEntity;
import com.loadup.order.repository.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderMappingTest {

    private final OrderMapping mapping = Mappers.getMapper(OrderMapping.class);

    @Test
    void shouldMapCreateRequestToOrderEntity() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomer(new Customer()
                .customerId("customer-1")
                .communicationPreference(CommunicationPreference.EMAIL)
                .email("customer-1@loadup.com"));
        request.setCurrency("USD");
        request.setItems(List.of(
                new OrderItem().sku("sku-1").name("Item 1").quantity(2).unitPrice(10.0),
                new OrderItem().sku("sku-2").name("Item 2").quantity(1).unitPrice(5.5)
        ));

        OrderEntity entity = mapping.toOrderEntity(request, "tenant-1", "order-1");

        assertEquals("tenant-1", entity.getTenantId());
        assertEquals("order-1", entity.getOrderId());
        assertEquals("customer-1", entity.getCustomerId());
        assertEquals("USD", entity.getCurrency());
        assertNotNull(entity.getCreatedTime());
        assertNotNull(entity.getUpdatedTime());
        assertEquals(25.5d, entity.getTotalAmount(), 0.0001);
        assertEquals(2, entity.getItems().size());
        assertSame(entity, entity.getItems().getFirst().getOrder());
    }

    @Test
    void shouldUpdateStatusAndUpdatedTimeFromUpdateRequest() {
        OrderEntity entity = OrderEntity.builder()
                .tenantId("tenant-1")
                .orderId("order-1")
                .customerId("customer-1")
                .currency("USD")
                .status(OrderStatus.CREATED)
                .totalAmount(10.0)
                .createdTime(LocalDateTime.now().minusHours(2))
                .updatedTime(LocalDateTime.now().minusHours(1))
                .items(List.of())
                .build();

        LocalDateTime beforeUpdate = entity.getUpdatedTime();

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setTargetStatus(com.loadup.order.dto.OrderStatus.SHIPPED);

        mapping.updateOrderEntity(request, entity);

        assertEquals(com.loadup.order.repository.model.OrderStatus.SHIPPED, entity.getStatus());
        assertNotNull(entity.getUpdatedTime());
        assertTrue(entity.getUpdatedTime().isAfter(beforeUpdate));
    }
}
