package com.spring.digital_logistics.repository;

import com.spring.digital_logistics.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder,Long> {
    List<SalesOrder> findAllByClientId(Long clientId);
    boolean existsByProductId(Long productId);
}
