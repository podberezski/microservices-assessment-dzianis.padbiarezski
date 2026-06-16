package com.loadup.notification.dto;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Order {


    private Long id;

    private String orderId;

    private String currency;

    private double totalAmount;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    private Customer customer;

    private List<OrderItemEntity> items = new ArrayList<>();

    @Data
    public class OrderItemEntity {

        private Long id;

        private String sku;

        private String name;

        private int quantity;

        private double unitPrice;

    }

}
