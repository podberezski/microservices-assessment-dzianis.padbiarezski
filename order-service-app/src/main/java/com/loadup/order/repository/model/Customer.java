package com.loadup.order.repository.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Customer {

    private String customerId;

    private CommunicationPreference communicationPreference;

    private String address;

    private String email;

    @Getter
    public enum CommunicationPreference {

        EMAIL("EMAIL"),

        MAIL("MAIL");

        private String value;

        CommunicationPreference(String value) {
            this.value = value;
        }

    }
}