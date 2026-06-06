package com.system.flipkartminutes.entities;

import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Data
@ToString
public class Customer {
    private String customerId;
    private String customerName;
    private String phoneNumber;
    private Address address;
    private boolean flipkartOne;

    public Customer(String customerName, String phoneNumber, Address address, boolean flipkartOne) {
        this.customerId = UUID.randomUUID().toString();
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.flipkartOne = flipkartOne;
    }
}
