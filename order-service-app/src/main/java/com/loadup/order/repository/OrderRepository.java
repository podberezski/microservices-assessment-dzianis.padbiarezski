package com.loadup.order.repository;

import com.loadup.order.repository.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {


    Optional<OrderEntity> findByTenantIdAndOrderId(String tenantId, String orderId);

    List<OrderEntity> findByTenantId(String tenantId);

    List<OrderEntity> findByCustomerId(String customerId);

    List<OrderEntity> findByTenantIdAndCustomerId(String tenantId, String customerId);

}
