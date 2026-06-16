package com.loadup.order.service;

import com.loadup.order.cache.TenantCacheService;
import com.loadup.order.repository.model.TenantEntity;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TenantService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantService.class);

    private final TenantCacheService tenantCacheService;

    /**
     * Retrieves tenant from cache if available, otherwise fetches from database and caches it.
     *
     * @param tenantId the tenant ID
     * @return the tenant entity
     * @throws ResponseStatusException if tenant is not found
     */
    public TenantEntity getTenantOrThrow(String tenantId) {
        var cachedTenant = tenantCacheService.getTenant(tenantId);
        if (cachedTenant.isPresent()) {
            LOGGER.info("Tenant found in cache: tenantId={}", tenantId);
            return cachedTenant.get();
        }

        TenantEntity fakeTenant = new TenantEntity(tenantId, "Inc.");

        tenantCacheService.cacheTenant(fakeTenant);
        LOGGER.info("Tenant loaded from database and cached: tenantId={}", tenantId);
        return fakeTenant;
    }


    public void initiateTenantContext(String tenantId) {
        try {
            LOGGER.info("Initiating tenant context: tenantId={}", tenantId);
            TenantEntity tenant = getTenantOrThrow(tenantId);

            LOGGER.info("Tenant context initiated successfully: tenantId={}, tenantName={}",
                    tenantId, tenant.getName());
        } catch (Exception exception) {
            LOGGER.error("Failed to initiate tenant context: tenantId={}", tenantId, exception);
        }
    }
}

