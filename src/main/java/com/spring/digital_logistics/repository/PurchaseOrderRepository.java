package com.spring.digital_logistics.repository;

import com.spring.digital_logistics.entity.PurchaseOrder;
import com.spring.digital_logistics.entity.enums.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder,Long> {
    public List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);
}
