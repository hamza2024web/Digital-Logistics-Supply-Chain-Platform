package com.spring.digital_logistics.repository;

import com.spring.digital_logistics.entity.SalesOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine,Long> {
}
