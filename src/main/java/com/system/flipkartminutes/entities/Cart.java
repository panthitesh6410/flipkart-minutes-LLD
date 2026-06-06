package com.system.flipkartminutes.entities;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@ToString
@Slf4j
public class Cart {

    private String cartId;
    private String customerId;
    private List<Product> items;

    public Cart(String customerId) {
        this.cartId = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.items = new ArrayList<>();
    }

    public void addItem(Product product) {
        log.info("Adding item to cart {}", product);
        items.add(product);
    }
    public void removeItem(Product product) {
        log.info("Removing item from cart {}", product);
        items.remove(product);
    }


}
