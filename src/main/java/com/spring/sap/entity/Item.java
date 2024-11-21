package com.spring.sap.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Item {

    @Id
    @Column(name = "item_id")
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String category;

    @Column(nullable = true)
    private String description;

    @Column(nullable = true)
    private Double price;

    @Column(name = "stock_quantity", nullable = true)
    private Integer stockQuantity;

    private String maker;

    private String parts;

    private String performance;

    @Column(name = "purchase_price")
    private Double purchasePrice;

    @Column(name = "sell_price")
    private Double sellPrice;

    public Item() {
    }
}
