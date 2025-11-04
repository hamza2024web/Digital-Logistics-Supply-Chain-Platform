package com.spring.digital_logistics.exception;

public class StockUnavailableException extends RuntimeException {
    public StockUnavailableException(String message) {
        super(message);
    }
}
