package com.loadup.notification.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderRedisCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderRedisCacheService.class);
    private static final String ORDERS_CACHE = CacheConfiguration.ORDERS_CACHE;

    private final CacheManager cacheManager;

    public OrderRedisCacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public Optional<String> getOrder(String tenantId, String orderId) {
        try {
            Cache cache = cacheManager.getCache(ORDERS_CACHE);
            if (cache == null) {
                throw new IllegalStateException("Cache '" + ORDERS_CACHE + "' is not configured");
            }

            String cacheKey = buildCacheKey(tenantId, orderId);
            Cache.ValueWrapper valueWrapper = cache.get(cacheKey);

            if (valueWrapper == null) {
                LOGGER.debug("Order is not present in cache: tenantId={}, orderId={}", tenantId, orderId);
                return Optional.empty();
            }

            return Optional.ofNullable((String) valueWrapper.get());
        } catch ( Exception e) {
            // TODO: Redis cache is not configured yet
            LOGGER.info("Order is not present in cache: tenantId={}, orderId={}", tenantId, orderId);
            return Optional.empty();
        }
    }

    String buildCacheKey(String tenantId, String orderId) {
        return tenantId + ":" + orderId;
    }
}
