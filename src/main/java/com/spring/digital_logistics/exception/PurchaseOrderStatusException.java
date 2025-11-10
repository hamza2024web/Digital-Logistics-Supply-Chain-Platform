package com.spring.digital_logistics.exception;

public class PurchaseOrderStatusException extends RuntimeException {
    public PurchaseOrderStatusException(String message) {
        super(message);
    }
}
