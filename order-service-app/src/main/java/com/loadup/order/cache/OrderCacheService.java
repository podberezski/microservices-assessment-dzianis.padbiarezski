package com.loadup.order.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loadup.order.repository.model.OrderEntity;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.log4j.Log4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class OrderCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderCacheService.class);
    private static final String ORDER_CACHE_CIRCUIT_BREAKER = "orderCacheCircuitBreaker";
    private static final String ORDER_CACHE_RETRY = "orderCacheRetry";

    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    public OrderCacheService(CacheManager cacheManager, ObjectMapper objectMapper) {
        this.cacheManager = cacheManager;
        this.objectMapper = objectMapper;
    }

    @Retry(name = ORDER_CACHE_RETRY, fallbackMethod = "cacheOrderFallback")
    @CircuitBreaker(name = ORDER_CACHE_CIRCUIT_BREAKER)
    public void cacheOrder(OrderEntity orderEntity) {
        Cache cache = cacheManager.getCache(CacheConfiguration.ORDERS_CACHE);
        if (cache == null) {
            throw new IllegalStateException("Cache '" + CacheConfiguration.ORDERS_CACHE + "' is not configured");
        }

        try {
            String cacheKey = buildCacheKey(orderEntity.getTenantId(), orderEntity.getOrderId());
            String json = objectMapper.writeValueAsString(orderEntity);
            cache.put(cacheKey, json);
        } catch (JsonProcessingException exception)
        {
            LOGGER.warn("Failed to serialize order entity for caching: tenantId={}, orderId={}",
                    orderEntity.getTenantId(), orderEntity.getOrderId(), exception);
            throw new IllegalStateException("Failed to serialize order entity for caching", exception);
        }
    }

    void cacheOrderFallback(OrderEntity orderEntity, Throwable throwable) {
        String tenantId = orderEntity != null ? orderEntity.getTenantId() : "unknown";
        String orderId = orderEntity != null ? orderEntity.getOrderId() : "unknown";
        LOGGER.warn(
                "Skipping cache write for tenantId={}, orderId={} due to cache failure: {}",
                tenantId,
                orderId,
                throwable.toString()
        );
    }

    String buildCacheKey(String tenantId, String orderId) {
        return tenantId + ":" + orderId;
    }
}


