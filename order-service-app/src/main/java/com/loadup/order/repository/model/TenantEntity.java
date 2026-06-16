package com.loadup.order.repository.model;


import lombok.AllArgsConstructor;
import lombok.Data;

// Just a fake tenant db pojo
@Data
@AllArgsConstructor
public class TenantEntity {

    private String tenantId;

    private String name;

}

