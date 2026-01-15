package com.spring.digital_logistics.repository;

import com.spring.digital_logistics.entity.SalesOrderLine;
import com.spring.digital_logistics.entity.enums.SalesOrderLineStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine,Long> {
    List<SalesOrderLine> findByStatus(SalesOrderLineStatus status);
    boolean existsByProductId(Long product_id);
}
