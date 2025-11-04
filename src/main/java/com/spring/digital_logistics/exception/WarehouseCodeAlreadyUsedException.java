package com.spring.digital_logistics.exception;

public class WarehouseCodeAlreadyUsedException extends RuntimeException {
    public WarehouseCodeAlreadyUsedException(String message) {
        super(message);
    }
}
