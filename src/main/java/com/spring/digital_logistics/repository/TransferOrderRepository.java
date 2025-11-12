package com.spring.digital_logistics.repository;

import com.spring.digital_logistics.entity.Product;
import com.spring.digital_logistics.entity.TransferOrder;
import com.spring.digital_logistics.entity.Warehouse;
import com.spring.digital_logistics.entity.enums.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferOrderRepository extends JpaRepository<TransferOrder,Long> {
    List<TransferOrder> findByStatus(TransferStatus status);
    boolean existsByProductAndDestinationWarehouseAndStatus(Product product , Warehouse warehouse , TransferStatus status);
}
