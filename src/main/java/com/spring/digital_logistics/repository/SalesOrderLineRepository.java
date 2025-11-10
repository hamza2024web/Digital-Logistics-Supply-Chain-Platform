package com.spring.digital_logistics.repository;

import com.spring.digital_logistics.entity.SalesOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine,Long> {
    SalesOrderLine findByProductId(Long id);
}
