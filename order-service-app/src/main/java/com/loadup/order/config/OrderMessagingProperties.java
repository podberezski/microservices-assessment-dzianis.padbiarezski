package com.loadup.order.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.kafka.order-events")
public class OrderMessagingProperties {

    private String bindingName = "orderEvents-out-0";
}

