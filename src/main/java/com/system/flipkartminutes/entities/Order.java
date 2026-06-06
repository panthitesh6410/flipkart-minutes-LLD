package com.system.flipkartminutes.entities;


import com.system.flipkartminutes.enums.OrderStatus;
import com.system.flipkartminutes.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.List;

@Builder
@Data
@ToString
public class Order {

    private String orderId;
    private Customer customer;
    private List<Product> products;
    private double totalAmount;
    private DeliveryAgent deliveryAgent;
    private OrderStatus orderStatus;
    private Timestamp orderPlaceTime;
    private PaymentStatus paymentStatus;

}
