package com.system.flipkartminutes.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class Product {
    private String productId;
    private String productName;
    private String description;
    private double price;

    public Product(String productName, String description, double price) {
        this.productId = java.util.UUID.randomUUID().toString();
        this.productName = productName;
        this.description = description;
        this.price = price;
    }
}
