package com.system.flipkartminutes.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class Address {
    private String flatNumber;
    private String street;
    private String city;
    private String state;
    private String pinCode;
    private String country;
    private String landMark;
}
