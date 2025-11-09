package com.spring.digital_logistics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_order_line")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;
    private BigDecimal price;
}
