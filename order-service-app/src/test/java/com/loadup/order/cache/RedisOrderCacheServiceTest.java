package com.loadup.order.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loadup.order.repository.model.OrderEntity;
import com.loadup.order.repository.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisOrderCacheServiceTest {

    private final ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(CacheConfiguration.ORDERS_CACHE);
    private final OrderCacheService cacheService = new OrderCacheService(cacheManager, new ObjectMapper());

    @Test
    void shouldPersistSerializedOrderInCache() {
        OrderEntity orderEntity = OrderEntity.builder()
                .tenantId("tenant-1")
                .orderId("order-1")
                .customerId("customer-1")
                .status(OrderStatus.CREATED)
                .currency("USD")
                .totalAmount(12.34)
                .build();

        cacheService.cacheOrder(orderEntity);

        Cache cache = cacheManager.getCache(CacheConfiguration.ORDERS_CACHE);
        assertNotNull(cache);

        String cachedJson = cache.get("tenant-1:order-1", String.class);
        assertNotNull(cachedJson);
        assertTrue(cachedJson.contains("\"tenantId\":\"tenant-1\""));
        assertTrue(cachedJson.contains("\"orderId\":\"order-1\""));
    }
}


