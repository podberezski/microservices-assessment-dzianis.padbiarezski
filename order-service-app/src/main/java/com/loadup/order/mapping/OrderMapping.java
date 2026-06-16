package com.loadup.order.mapping;

import com.loadup.order.dto.CreateOrderRequest;
import com.loadup.order.dto.OrderResponse;
import com.loadup.order.dto.UpdateOrderRequest;
import com.loadup.order.repository.model.OrderEntity;
import com.loadup.order.repository.model.OrderItemEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface OrderMapping {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", source = "tenantId")
    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "customerId", source = "request.customer.customerId")
    @Mapping(target = "currency", source = "request.currency")
    @Mapping(target = "createdTime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedTime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "items", source = "request.items")
    @Mapping(target = "customer", source = "request.customer")
    @Mapping(target = "status", ignore = true)
    OrderEntity toOrderEntity(CreateOrderRequest request, String tenantId, String orderId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    OrderItemEntity toOrderItemEntity(com.loadup.order.dto.OrderItem item);

    com.loadup.order.dto.OrderItem toOrderItem(com.loadup.order.repository.model.OrderItemEntity itemEntity);

    com.loadup.order.repository.model.OrderStatus toOrderStatus(com.loadup.order.dto.OrderStatus orderStatus);

    com.loadup.order.dto.OrderStatus toDtoOrderStatus(com.loadup.order.repository.model.OrderStatus orderStatus);

    @Mapping(target = "createdAt", source = "createdTime")
    @Mapping(target = "updatedAt", source = "updatedTime")
    OrderResponse toOrderResponse(OrderEntity orderEntity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "status", source = "targetStatus")
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "updatedTime", expression = "java(java.time.LocalDateTime.now())")
    void updateOrderEntity(UpdateOrderRequest request, @MappingTarget OrderEntity orderEntity);

    @AfterMapping
    default void enrichAfterCreate(@MappingTarget OrderEntity orderEntity) {
        if (orderEntity.getItems() != null) {
            orderEntity.getItems().forEach(item -> item.setOrder(orderEntity));
            double total = orderEntity.getItems().stream()
                    .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                    .sum();
            orderEntity.setTotalAmount(total);
        } else {
            orderEntity.setTotalAmount(0.0d);
        }
    }

    @AfterMapping
    default void ensureUpdatedTime(@MappingTarget OrderEntity orderEntity, UpdateOrderRequest request) {
        orderEntity.setUpdatedTime(LocalDateTime.now());
    }

    default OffsetDateTime map(LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}
