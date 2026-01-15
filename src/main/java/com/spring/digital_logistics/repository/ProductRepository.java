package com.spring.digital_logistics.repository;

import com.spring.digital_logistics.entity.Product;
import org.springframework.data.jpa. repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    List<Product> findBySkuContainingIgnoreCaseOrNameContainingIgnoreCase(String sku, String name);

    List<Product> findByActiveTrue();

    List<Product> findByActive(boolean active);
}