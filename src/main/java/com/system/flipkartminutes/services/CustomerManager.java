package com.system.flipkartminutes.services;


import com.system.flipkartminutes.entities.Customer;
import com.system.flipkartminutes.exceptions.CustomerNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CustomerManager {

    // Singleton class (without springboot annorations like @Component or @Service)
    private CustomerManager(){}

    private static CustomerManager customerManager;

    public static CustomerManager getInstance(){
        if(customerManager == null){
            synchronized (CustomerManager.class){
                if(customerManager == null){
                    customerManager = new CustomerManager();
                }
            }
        }
        return customerManager;
    }

    private Map<String, Customer> customersById;

    public void addCustomer(Customer customer){
        if(customersById == null){
            customersById = new ConcurrentHashMap<>();
        }
        customersById.put(customer.getCustomerId(), customer);
        log.info("Customer created: {} - {}", customer.getCustomerId(), customer.getCustomerName());
    }

    public Customer getCustomerById(String customerId) throws CustomerNotFoundException{
        if(customersById == null){
            customersById = new ConcurrentHashMap<>();
        }
        if(!customersById.containsKey(customerId)){
            throw new CustomerNotFoundException("Customer not found with id: " + customerId);
        }
        return customersById.get(customerId);
    }

}
