package com.spring.digital_logistics.entity;

import com.spring.digital_logistics.entity.enums.SalesOrderLineStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_order_line")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "salesOrder")
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalesOrderLineStatus status;
}
