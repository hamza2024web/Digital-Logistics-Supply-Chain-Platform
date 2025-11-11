package com.spring.digital_logistics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory" , uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id","warehouse_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    private int qtyOnHand;

    private int qtyReserved;

    public Inventory(Product product, Warehouse warehouse, int qtyOnHand, int qtyReserved) {
        this.product = product;
        this.warehouse = warehouse;
        this.qtyOnHand = qtyOnHand;
        this.qtyReserved = qtyReserved;
    }
}
