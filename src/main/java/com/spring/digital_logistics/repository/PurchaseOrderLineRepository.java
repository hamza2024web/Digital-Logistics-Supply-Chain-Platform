package com.spring.digital_logistics.repository;

import com.spring.digital_logistics.entity.POLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderLineRepository extends JpaRepository<POLine,Long> {

}
