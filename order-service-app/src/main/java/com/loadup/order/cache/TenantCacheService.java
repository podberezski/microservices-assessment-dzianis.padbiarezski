package com.loadup.order.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loadup.order.repository.model.TenantEntity;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TenantCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantCacheService.class);
    private static final String TENANT_CACHE_CIRCUIT_BREAKER = "tenantCacheCircuitBreaker";
    private static final String TENANT_CACHE_RETRY = "tenantCacheRetry";

    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    public TenantCacheService(CacheManager cacheManager, ObjectMapper objectMapper) {
        this.cacheManager = cacheManager;
        this.objectMapper = objectMapper;
    }

    @Retry(name = TENANT_CACHE_RETRY, fallbackMethod = "getTenantFallback")
    @CircuitBreaker(name = TENANT_CACHE_CIRCUIT_BREAKER)
    public Optional<TenantEntity> getTenant(String tenantId) {
        Cache cache = cacheManager.getCache(CacheConfiguration.TENANTS_CACHE);
        if (cache == null) {
            throw new IllegalStateException("Cache '" + CacheConfiguration.TENANTS_CACHE + "' is not configured");
        }

        Cache.ValueWrapper valueWrapper = cache.get(tenantId);
        if (valueWrapper == null) {
            return Optional.empty();
        }

        try {
            String json = (String) valueWrapper.get();
            TenantEntity tenantEntity = objectMapper.readValue(json, TenantEntity.class);
            return Optional.of(tenantEntity);
        } catch (JsonProcessingException exception) {
            LOGGER.warn("Failed to deserialize cached tenant: tenantId={}", tenantId, exception);
            cache.evict(tenantId);
            return Optional.empty();
        }
    }

    @Retry(name = TENANT_CACHE_RETRY, fallbackMethod = "cacheTenantFallback")
    @CircuitBreaker(name = TENANT_CACHE_CIRCUIT_BREAKER)
    public void cacheTenant(TenantEntity tenantEntity) {
        Cache cache = cacheManager.getCache(CacheConfiguration.TENANTS_CACHE);
        if (cache == null) {
            throw new IllegalStateException("Cache '" + CacheConfiguration.TENANTS_CACHE + "' is not configured");
        }

        try {
            String json = objectMapper.writeValueAsString(tenantEntity);
            cache.put(tenantEntity.getTenantId(), json);
        } catch (JsonProcessingException exception) {
            LOGGER.warn("Failed to serialize tenant entity for caching: tenantId={}", tenantEntity.getTenantId(), exception);
            throw new IllegalStateException("Failed to serialize tenant entity for caching", exception);
        }
    }

    Optional<TenantEntity> getTenantFallback(String tenantId, Throwable throwable) {
        LOGGER.warn("Failed to retrieve tenant from cache: tenantId={}, error={}", tenantId, throwable.toString());
        return Optional.empty();
    }

    void cacheTenantFallback(TenantEntity tenantEntity, Throwable throwable) {
        String tenantId = tenantEntity != null ? tenantEntity.getTenantId() : "unknown";
        LOGGER.warn(
                "Skipping cache write for tenantId={} due to cache failure: {}",
                tenantId,
                throwable.toString()
        );
    }
}

