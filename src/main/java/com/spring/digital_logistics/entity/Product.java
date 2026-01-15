package com.spring.digital_logistics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    private String image;

    private BigDecimal price;


    private Boolean active = true;

    public Product(String sku , String name , String image , BigDecimal price , boolean active){
        this.sku = sku;
        this.name = name;
        this.image = image;
        this.price = price;
        this.active = active;
    }
}
