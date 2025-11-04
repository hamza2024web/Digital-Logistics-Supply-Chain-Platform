package com.spring.digital_logistics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "warehouse")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false , unique = true)
    private String code;

    @Column(nullable = false)
    private String name;
}
