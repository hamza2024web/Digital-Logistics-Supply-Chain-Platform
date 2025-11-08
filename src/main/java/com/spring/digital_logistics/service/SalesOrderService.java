package com.spring.digital_logistics.service;

import com.spring.digital_logistics.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;

@Service
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;

    public SalesOrderService(SalesOrderRepository salesOrderRepository) {
        this.salesOrderRepository = salesOrderRepository;
    }
}
