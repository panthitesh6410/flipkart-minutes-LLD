package com.system.flipkartminutes;

import com.system.flipkartminutes.entities.*;
import com.system.flipkartminutes.facade.OrderFacade;
import com.system.flipkartminutes.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class FlipkartMinutesApplication {

    public static void main(String[] args) {
        // SpringApplication.run(FlipkartMinutesApplication.class, args);

      log.info("Starting FlipkartMinutesApplication");

      // Prerequirestes ----------------------------------------------

        Product apple = new Product("Apple", "Fresh red apple", 10.0);
        Product banana = new Product("Banana", "Ripe yellow banana", 5.0);
        Product orange = new Product("Orange", "Juicy orange", 8.0);

        ProductRepository productRepository = ProductRepository.getInstance();
        productRepository.addProduct(apple);
        productRepository.addProduct(banana);
        productRepository.addProduct(orange);

        InventoryManager inventoryManager = InventoryManager.getInstance();
        inventoryManager.addInventory(apple.getProductId(), 100);
        inventoryManager.addInventory(banana.getProductId(), 150);
        inventoryManager.addInventory(orange.getProductId(), 120);

        Customer hitesh = new Customer("Hitesh", "8989438834", new Address("flat-1", "maathalli", "banfalire", "karnatake", "560909", "India", "neaer mulitplex"), true);
        Customer hari = new Customer("Hari", "8989438835", new Address("flat-2", "maathalli", "banfalire", "karnatake", "560909", "India", "neaer mulitplex"), true);
        CustomerManager customerManager = CustomerManager.getInstance();
        customerManager.addCustomer(hitesh);
        customerManager.addCustomer(hari);


        CartManager cartManager = CartManager.getInstance();
        Cart hiteshCart = cartManager.createCart(hitesh.getCustomerId());
        hiteshCart.addItem(apple);
        Cart hariCart = cartManager.createCart(hari.getCustomerId());
        hariCart.addItem(banana);

      //  -------------------------------------------------------------


      OrderFacade orderFacade = OrderFacade.getInstance();
      orderFacade.placeOrder(hiteshCart);   // shoue throw no felivery agent here foundddd

      DeliveryAgent agent = new DeliveryAgent("Anil", "89898989",  new Address("flat-1", "maathalli", "banfalire", "karnatake", "560909", "India", "neaer mulitplex"));
      DeliveryAgentManager deliveryAgentManager = DeliveryAgentManager.getInstance();
      deliveryAgentManager.addDeliveryAgent(agent);

      ///  again trying...
      orderFacade.placeOrder(hiteshCart);   // shoue throw no felivery agent here foundddd


      // view user history:
      orderFacade.viewHistory(hitesh.getCustomerId());

    }




}
