package com.loadup.notification.service;

public enum OrderStatus {

    CREATED,
    UPDATED,
    COMPLETED,
    CANCELLED,
    FAILED;

    public boolean isReceiptTrigger() {
        return this == CREATED || this == UPDATED;
    }

    public boolean isTerminalState() {
        return this == COMPLETED || this == CANCELLED || this == FAILED;
    }
}

