package com.spring.digital_logistics.repository;

import com.spring.digital_logistics.entity.Inventory;
import com.spring.digital_logistics.entity.Product;
import com.spring.digital_logistics.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductAndWarehouse(Product product, Warehouse warehouse);
    Inventory findByProductId(Long productId);
}
